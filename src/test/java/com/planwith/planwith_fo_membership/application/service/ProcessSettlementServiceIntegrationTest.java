package com.planwith.planwith_fo_membership.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_membership.application.command.ApproveSettlementCommand;
import com.planwith.planwith_fo_membership.application.command.PaySettlementCommand;
import com.planwith.planwith_fo_membership.application.command.RejectSettlementCommand;
import com.planwith.planwith_fo_membership.application.command.RequestSettlementCommand;
import com.planwith.planwith_fo_membership.application.port.in.command.ApproveSettlementUseCase;
import com.planwith.planwith_fo_membership.application.port.in.command.PaySettlementUseCase;
import com.planwith.planwith_fo_membership.application.port.in.command.RejectSettlementUseCase;
import com.planwith.planwith_fo_membership.application.port.in.command.RequestSettlementUseCase;
import com.planwith.planwith_fo_membership.application.port.out.LoadRevenuePort;
import com.planwith.planwith_fo_membership.application.port.out.SaveRevenuePort;
import com.planwith.planwith_fo_membership.application.query.ProcessSettlementResult;
import com.planwith.planwith_fo_membership.application.query.RequestSettlementResult;
import com.planwith.planwith_fo_membership.domain.event.MembershipEventTypes;
import com.planwith.planwith_fo_membership.domain.model.MembershipRevenue;
import com.planwith.planwith_fo_membership.domain.model.SettlementStatus;
import com.planwith.planwith_fo_membership.domain.model.vo.AdminUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.RevenueUuid;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProcessSettlementServiceIntegrationTest {

	private final CreatorUuid creatorUuid = CreatorUuid.from("22222222-2222-2222-2222-222222222222");
	private final RevenueUuid revenueUuid = RevenueUuid.from("66666666-6666-6666-6666-666666666666");
	private final AdminUuid adminUuid = AdminUuid.from("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

	@Autowired
	private RequestSettlementUseCase requestSettlementUseCase;

	@Autowired
	private ApproveSettlementUseCase approveSettlementUseCase;

	@Autowired
	private RejectSettlementUseCase rejectSettlementUseCase;

	@Autowired
	private PaySettlementUseCase paySettlementUseCase;

	@Autowired
	private SaveRevenuePort saveRevenuePort;

	@Autowired
	private LoadRevenuePort loadRevenuePort;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void approveThenPayConfirmsReservedWithoutSecondAvailableDeduction() {
		saveRevenuePort.save(MembershipRevenue.empty(revenueUuid, creatorUuid).record(50_000L));
		RequestSettlementResult requested = requestSettlementUseCase.request(
				new RequestSettlementCommand(creatorUuid, 30_000L, Instant.parse("2026-08-22T01:00:00Z"))
		);

		approveSettlementUseCase.approve(new ApproveSettlementCommand(
				requested.settlementUuid(),
				adminUuid,
				Instant.parse("2026-08-22T01:10:00Z")
		));
		ProcessSettlementResult paid = paySettlementUseCase.pay(new PaySettlementCommand(
				requested.settlementUuid(),
				adminUuid,
				Instant.parse("2026-08-22T01:20:00Z")
		));

		MembershipRevenue revenue = loadRevenuePort.findByCreator(creatorUuid).orElseThrow();
		assertThat(paid.status()).isEqualTo(SettlementStatus.PAID);
		assertThat(revenue.availableRevenue()).isEqualTo(20_000L);
		assertThat(revenue.reservedRevenue()).isZero();
		assertThat(revenue.settledRevenue()).isEqualTo(30_000L);
		assertThat(eventTypes(requested.settlementUuid().value().toString()))
				.contains(MembershipEventTypes.SETTLEMENT_REQUESTED, MembershipEventTypes.SETTLEMENT_COMPLETED);
	}

	@Test
	void rejectReturnsReservedAmountToAvailable() {
		saveRevenuePort.save(MembershipRevenue.empty(revenueUuid, creatorUuid).record(50_000L));
		RequestSettlementResult requested = requestSettlementUseCase.request(
				new RequestSettlementCommand(creatorUuid, 30_000L, Instant.parse("2026-08-22T01:00:00Z"))
		);

		ProcessSettlementResult rejected = rejectSettlementUseCase.reject(
				new RejectSettlementCommand(requested.settlementUuid(), adminUuid, "계좌 정보 오류")
		);

		MembershipRevenue revenue = loadRevenuePort.findByCreator(creatorUuid).orElseThrow();
		assertThat(rejected.status()).isEqualTo(SettlementStatus.REJECTED);
		assertThat(revenue.availableRevenue()).isEqualTo(50_000L);
		assertThat(revenue.reservedRevenue()).isZero();
		assertThat(revenue.settledRevenue()).isZero();
	}

	private List<String> eventTypes(String settlementUuid) {
		return jdbcTemplate.queryForList(
				"select event_type from membership_outbox where aggregate_uuid = ? order by occurred_at",
				String.class,
				settlementUuid
		);
	}
}
