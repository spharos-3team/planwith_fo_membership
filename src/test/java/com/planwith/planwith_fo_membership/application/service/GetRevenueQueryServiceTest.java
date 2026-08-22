package com.planwith.planwith_fo_membership.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_membership.adapter.out.persistence.revenue.InMemoryRevenuePort;
import com.planwith.planwith_fo_membership.application.query.GetRevenueQuery;
import com.planwith.planwith_fo_membership.application.query.RevenueResult;
import com.planwith.planwith_fo_membership.domain.model.MembershipRevenue;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.RevenueUuid;

class GetRevenueQueryServiceTest {

	private final CreatorUuid creatorUuid = CreatorUuid.from("22222222-2222-2222-2222-222222222222");
	private final RevenueUuid revenueUuid = RevenueUuid.from("66666666-6666-6666-6666-666666666666");

	private InMemoryRevenuePort revenuePort;
	private GetRevenueQueryService service;

	@BeforeEach
	void setUp() {
		revenuePort = new InMemoryRevenuePort();
		service = new GetRevenueQueryService(revenuePort);
	}

	@Test
	void returnsZeroRevenueWhenAccountDoesNotExist() {
		RevenueResult result = service.get(new GetRevenueQuery(creatorUuid));

		assertThat(result.revenueUuid()).isNull();
		assertThat(result.creatorUuid()).isEqualTo(creatorUuid);
		assertThat(result.totalRevenue()).isZero();
		assertThat(result.availableRevenue()).isZero();
		assertThat(result.settledRevenue()).isZero();
	}

	@Test
	void returnsStoredRevenueAndSettlementAmount() {
		revenuePort.save(MembershipRevenue.empty(revenueUuid, creatorUuid).record(12900L).settle(2900L));

		RevenueResult result = service.get(new GetRevenueQuery(creatorUuid));

		assertThat(result.revenueUuid()).isEqualTo(revenueUuid);
		assertThat(result.totalRevenue()).isEqualTo(12900L);
		assertThat(result.availableRevenue()).isEqualTo(10000L);
		assertThat(result.settledRevenue()).isEqualTo(2900L);
	}
}
