package com.planwith.planwith_fo_membership.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_membership.application.command.RequestSettlementCommand;
import com.planwith.planwith_fo_membership.application.port.in.command.RequestSettlementUseCase;
import com.planwith.planwith_fo_membership.application.port.in.query.GetRevenueQueryUseCase;
import com.planwith.planwith_fo_membership.application.port.out.LoadRevenuePort;
import com.planwith.planwith_fo_membership.application.port.out.LoadSettlementPort;
import com.planwith.planwith_fo_membership.application.port.out.SaveRevenuePort;
import com.planwith.planwith_fo_membership.application.query.GetRevenueQuery;
import com.planwith.planwith_fo_membership.application.query.RequestSettlementResult;
import com.planwith.planwith_fo_membership.application.query.RevenueResult;
import com.planwith.planwith_fo_membership.domain.event.MembershipEventTypes;
import com.planwith.planwith_fo_membership.domain.exception.SettlementAmountExceededException;
import com.planwith.planwith_fo_membership.domain.model.MembershipRevenue;
import com.planwith.planwith_fo_membership.domain.model.MembershipSettlement;
import com.planwith.planwith_fo_membership.domain.model.SettlementStatus;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.RevenueUuid;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RequestSettlementServiceIntegrationTest {

	private final CreatorUuid creatorUuid = CreatorUuid.from("22222222-2222-2222-2222-222222222222");
	private final RevenueUuid revenueUuid = RevenueUuid.from("66666666-6666-6666-6666-666666666666");

	@Autowired
	private RequestSettlementUseCase requestSettlementUseCase;

	@Autowired
	private GetRevenueQueryUseCase getRevenueQueryUseCase;

	@Autowired
	private SaveRevenuePort saveRevenuePort;

	@Autowired
	private LoadRevenuePort loadRevenuePort;

	@Autowired
	private LoadSettlementPort loadSettlementPort;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void requestReservesAvailableAmountAndCreatesRequestedSettlement() {
		saveRevenuePort.save(MembershipRevenue.empty(revenueUuid, creatorUuid).record(50_000L));

		RequestSettlementResult result = requestSettlementUseCase.request(
				new RequestSettlementCommand(creatorUuid, 30_000L, Instant.parse("2026-08-22T01:00:00Z"))
		);

		MembershipSettlement settlement = loadSettlementPort.findByUuid(result.settlementUuid()).orElseThrow();
		MembershipRevenue revenue = loadRevenuePort.findByCreator(creatorUuid).orElseThrow();
		RevenueResult query = getRevenueQueryUseCase.get(new GetRevenueQuery(creatorUuid));
		assertThat(settlement.settlementStatus()).isEqualTo(SettlementStatus.REQUESTED);
		assertThat(settlement.settlementAmount()).isEqualTo(30_000L);
		assertThat(revenue.availableRevenue()).isEqualTo(20_000L);
		assertThat(revenue.reservedRevenue()).isEqualTo(30_000L);
		assertThat(revenue.settledRevenue()).isZero();
		assertThat(query.availableRevenue()).isEqualTo(20_000L);
		assertThat(query.reservedRevenue()).isEqualTo(30_000L);
		assertThat(query.settledRevenue()).isZero();
		assertThat(eventTypes(result.settlementUuid().value().toString()))
				.containsExactly(MembershipEventTypes.SETTLEMENT_REQUESTED);
	}

	@Test
	void rejectsDuplicateRequestForAlreadyReservedAmount() {
		saveRevenuePort.save(MembershipRevenue.empty(revenueUuid, creatorUuid).record(50_000L));
		requestSettlementUseCase.request(
				new RequestSettlementCommand(creatorUuid, 30_000L, Instant.parse("2026-08-22T01:00:00Z"))
		);

		assertThatThrownBy(() -> requestSettlementUseCase.request(
				new RequestSettlementCommand(creatorUuid, 30_000L, Instant.parse("2026-08-22T01:01:00Z"))
		)).isInstanceOf(SettlementAmountExceededException.class)
				.hasMessage("정산 가능 금액을 초과할 수 없습니다.");
	}

	private List<String> eventTypes(String settlementUuid) {
		return jdbcTemplate.queryForList(
				"select event_type from membership_outbox where aggregate_uuid = ?",
				String.class,
				settlementUuid
		);
	}
}
