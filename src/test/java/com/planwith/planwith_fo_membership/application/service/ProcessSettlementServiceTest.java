package com.planwith.planwith_fo_membership.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_membership.adapter.out.persistence.outbox.InMemoryMembershipEventOutboxPort;
import com.planwith.planwith_fo_membership.adapter.out.persistence.revenue.InMemoryRevenuePort;
import com.planwith.planwith_fo_membership.adapter.out.persistence.settlement.InMemorySettlementPort;
import com.planwith.planwith_fo_membership.application.command.ApproveSettlementCommand;
import com.planwith.planwith_fo_membership.application.command.PaySettlementCommand;
import com.planwith.planwith_fo_membership.application.command.RejectSettlementCommand;
import com.planwith.planwith_fo_membership.application.command.RequestSettlementCommand;
import com.planwith.planwith_fo_membership.application.query.ProcessSettlementResult;
import com.planwith.planwith_fo_membership.application.query.RequestSettlementResult;
import com.planwith.planwith_fo_membership.domain.event.MembershipEventTypes;
import com.planwith.planwith_fo_membership.domain.exception.SettlementAlreadyProcessedException;
import com.planwith.planwith_fo_membership.domain.exception.SettlementNotFoundException;
import com.planwith.planwith_fo_membership.domain.model.MembershipRevenue;
import com.planwith.planwith_fo_membership.domain.model.SettlementStatus;
import com.planwith.planwith_fo_membership.domain.model.vo.AdminUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.RevenueUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SettlementUuid;

class ProcessSettlementServiceTest {

	private final CreatorUuid creatorUuid = CreatorUuid.from("22222222-2222-2222-2222-222222222222");
	private final RevenueUuid revenueUuid = RevenueUuid.from("66666666-6666-6666-6666-666666666666");
	private final AdminUuid adminUuid = AdminUuid.from("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
	private final Instant requestedAt = Instant.parse("2026-08-22T01:00:00Z");
	private final Instant processedAt = Instant.parse("2026-08-22T01:10:00Z");

	private InMemoryRevenuePort revenuePort;
	private InMemorySettlementPort settlementPort;
	private InMemoryMembershipEventOutboxPort outboxPort;
	private RequestSettlementService requestSettlementService;
	private ProcessSettlementService processSettlementService;

	@BeforeEach
	void setUp() {
		revenuePort = new InMemoryRevenuePort();
		settlementPort = new InMemorySettlementPort();
		outboxPort = new InMemoryMembershipEventOutboxPort();
		ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
		requestSettlementService = new RequestSettlementService(
				revenuePort,
				revenuePort,
				settlementPort,
				outboxPort,
				objectMapper
		);
		processSettlementService = new ProcessSettlementService(
				settlementPort,
				settlementPort,
				revenuePort,
				revenuePort,
				outboxPort,
				objectMapper
		);
		revenuePort.save(MembershipRevenue.empty(revenueUuid, creatorUuid).record(50_000L));
	}

	@Test
	void approveDoesNotChangeReservedRevenue() {
		SettlementUuid settlementUuid = requestThirtyThousand().settlementUuid();

		ProcessSettlementResult result = processSettlementService.approve(
				new ApproveSettlementCommand(settlementUuid, adminUuid, processedAt)
		);

		MembershipRevenue revenue = revenuePort.findByCreator(creatorUuid).orElseThrow();
		assertThat(result.status()).isEqualTo(SettlementStatus.APPROVED);
		assertThat(result.adminUuid()).isEqualTo(adminUuid);
		assertThat(result.approvedAt()).isEqualTo(processedAt);
		assertThat(revenue.availableRevenue()).isEqualTo(20_000L);
		assertThat(revenue.reservedRevenue()).isEqualTo(30_000L);
		assertThat(revenue.settledRevenue()).isZero();
	}

	@Test
	void payConfirmsReservedAmountWithoutDeductingAvailableAgain() {
		SettlementUuid settlementUuid = requestThirtyThousand().settlementUuid();
		processSettlementService.approve(new ApproveSettlementCommand(settlementUuid, adminUuid, processedAt));

		ProcessSettlementResult result = processSettlementService.pay(
				new PaySettlementCommand(settlementUuid, adminUuid, Instant.parse("2026-08-22T01:20:00Z"))
		);

		MembershipRevenue revenue = revenuePort.findByCreator(creatorUuid).orElseThrow();
		assertThat(result.status()).isEqualTo(SettlementStatus.PAID);
		assertThat(result.settledRevenue()).isEqualTo(30_000L);
		assertThat(revenue.availableRevenue()).isEqualTo(20_000L);
		assertThat(revenue.reservedRevenue()).isZero();
		assertThat(revenue.settledRevenue()).isEqualTo(30_000L);
		assertThat(revenue.totalRevenue()).isEqualTo(50_000L);
		assertThat(outboxPort.messages().stream().map(message -> message.eventType()))
				.contains(MembershipEventTypes.SETTLEMENT_COMPLETED);
	}

	@Test
	void rejectReleasesReservedAmountToAvailable() {
		SettlementUuid settlementUuid = requestThirtyThousand().settlementUuid();

		ProcessSettlementResult result = processSettlementService.reject(
				new RejectSettlementCommand(settlementUuid, adminUuid, "계좌 정보 오류")
		);

		MembershipRevenue revenue = revenuePort.findByCreator(creatorUuid).orElseThrow();
		assertThat(result.status()).isEqualTo(SettlementStatus.REJECTED);
		assertThat(result.rejectReason()).isEqualTo("계좌 정보 오류");
		assertThat(revenue.availableRevenue()).isEqualTo(50_000L);
		assertThat(revenue.reservedRevenue()).isZero();
		assertThat(revenue.settledRevenue()).isZero();
	}

	@Test
	void rejectsPayOnRequestedAndSecondPayOnPaid() {
		SettlementUuid settlementUuid = requestThirtyThousand().settlementUuid();

		assertThatThrownBy(() -> processSettlementService.pay(
				new PaySettlementCommand(settlementUuid, adminUuid, processedAt)
		)).isInstanceOf(SettlementAlreadyProcessedException.class);

		processSettlementService.approve(new ApproveSettlementCommand(settlementUuid, adminUuid, processedAt));
		processSettlementService.pay(new PaySettlementCommand(settlementUuid, adminUuid, processedAt));

		assertThatThrownBy(() -> processSettlementService.pay(
				new PaySettlementCommand(settlementUuid, adminUuid, processedAt)
		)).isInstanceOf(SettlementAlreadyProcessedException.class);
	}

	@Test
	void rejectsMissingSettlement() {
		assertThatThrownBy(() -> processSettlementService.approve(new ApproveSettlementCommand(
				SettlementUuid.from("77777777-7777-7777-7777-777777777777"),
				adminUuid,
				processedAt
		))).isInstanceOf(SettlementNotFoundException.class);
	}

	private RequestSettlementResult requestThirtyThousand() {
		return requestSettlementService.request(new RequestSettlementCommand(creatorUuid, 30_000L, requestedAt));
	}
}
