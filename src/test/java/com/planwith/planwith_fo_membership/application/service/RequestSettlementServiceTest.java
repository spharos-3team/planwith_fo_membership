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
import com.planwith.planwith_fo_membership.application.command.RequestSettlementCommand;
import com.planwith.planwith_fo_membership.application.query.RequestSettlementResult;
import com.planwith.planwith_fo_membership.domain.event.MembershipEventTypes;
import com.planwith.planwith_fo_membership.domain.exception.InvalidRevenueException;
import com.planwith.planwith_fo_membership.domain.exception.InvalidSettlementStateException;
import com.planwith.planwith_fo_membership.domain.exception.SettlementAmountExceededException;
import com.planwith.planwith_fo_membership.domain.model.MembershipRevenue;
import com.planwith.planwith_fo_membership.domain.model.MembershipSettlement;
import com.planwith.planwith_fo_membership.domain.model.SettlementStatus;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.RevenueUuid;

class RequestSettlementServiceTest {

	private final CreatorUuid creatorUuid = CreatorUuid.from("22222222-2222-2222-2222-222222222222");
	private final RevenueUuid revenueUuid = RevenueUuid.from("66666666-6666-6666-6666-666666666666");
	private final Instant requestedAt = Instant.parse("2026-08-22T01:00:00Z");

	private InMemoryRevenuePort revenuePort;
	private InMemorySettlementPort settlementPort;
	private InMemoryMembershipEventOutboxPort outboxPort;
	private RequestSettlementService service;

	@BeforeEach
	void setUp() {
		revenuePort = new InMemoryRevenuePort();
		settlementPort = new InMemorySettlementPort();
		outboxPort = new InMemoryMembershipEventOutboxPort();
		service = new RequestSettlementService(
				revenuePort,
				revenuePort,
				settlementPort,
				outboxPort,
				new ObjectMapper().findAndRegisterModules()
		);
	}

	@Test
	void reservesAvailableRevenueAndCreatesRequestedSettlement() {
		revenuePort.save(MembershipRevenue.empty(revenueUuid, creatorUuid).record(50_000L));

		RequestSettlementResult result = service.request(
				new RequestSettlementCommand(creatorUuid, 30_000L, requestedAt)
		);

		MembershipRevenue reserved = revenuePort.findByCreator(creatorUuid).orElseThrow();
		MembershipSettlement settlement = settlementPort.findByUuid(result.settlementUuid()).orElseThrow();
		assertThat(result.status()).isEqualTo(SettlementStatus.REQUESTED);
		assertThat(result.settlementAmount()).isEqualTo(30_000L);
		assertThat(result.availableRevenue()).isEqualTo(20_000L);
		assertThat(result.reservedRevenue()).isEqualTo(30_000L);
		assertThat(reserved.availableRevenue()).isEqualTo(20_000L);
		assertThat(reserved.reservedRevenue()).isEqualTo(30_000L);
		assertThat(reserved.settledRevenue()).isZero();
		assertThat(reserved.totalRevenue()).isEqualTo(50_000L);
		assertThat(settlement.settlementStatus()).isEqualTo(SettlementStatus.REQUESTED);
		assertThat(settlement.settlementAmount()).isEqualTo(30_000L);
		assertThat(outboxPort.messages()).hasSize(1);
		assertThat(outboxPort.messages().get(0).eventType()).isEqualTo(MembershipEventTypes.SETTLEMENT_REQUESTED);
	}

	@Test
	void rejectsSecondRequestThatExceedsRemainingAvailable() {
		revenuePort.save(MembershipRevenue.empty(revenueUuid, creatorUuid).record(50_000L));
		service.request(new RequestSettlementCommand(creatorUuid, 30_000L, requestedAt));

		assertThatThrownBy(() -> service.request(new RequestSettlementCommand(creatorUuid, 30_000L, requestedAt)))
				.isInstanceOf(SettlementAmountExceededException.class)
				.hasMessage("정산 가능 금액을 초과할 수 없습니다.");
		assertThat(settlementPort.settlements()).hasSize(1);
		assertThat(outboxPort.messages()).hasSize(1);
	}

	@Test
	void allowsSecondRequestForRemainingAvailableRevenue() {
		revenuePort.save(MembershipRevenue.empty(revenueUuid, creatorUuid).record(50_000L));
		service.request(new RequestSettlementCommand(creatorUuid, 30_000L, requestedAt));

		RequestSettlementResult second = service.request(
				new RequestSettlementCommand(creatorUuid, 20_000L, requestedAt)
		);

		MembershipRevenue reserved = revenuePort.findByCreator(creatorUuid).orElseThrow();
		assertThat(second.settlementAmount()).isEqualTo(20_000L);
		assertThat(reserved.availableRevenue()).isZero();
		assertThat(reserved.reservedRevenue()).isEqualTo(50_000L);
		assertThat(settlementPort.settlements()).hasSize(2);
	}

	@Test
	void rejectsWhenRevenueAccountIsMissing() {
		assertThatThrownBy(() -> service.request(new RequestSettlementCommand(creatorUuid, 30_000L, requestedAt)))
				.isInstanceOf(InvalidRevenueException.class)
				.hasMessage("정산 가능한 수익이 없습니다.");
		assertThat(settlementPort.settlements()).isEmpty();
	}

	@Test
	void rejectsNonPositiveAmount() {
		revenuePort.save(MembershipRevenue.empty(revenueUuid, creatorUuid).record(50_000L));

		assertThatThrownBy(() -> service.request(new RequestSettlementCommand(creatorUuid, 0L, requestedAt)))
				.isInstanceOf(InvalidSettlementStateException.class)
				.hasMessage("정산 금액은 0보다 커야 합니다.");
	}
}
