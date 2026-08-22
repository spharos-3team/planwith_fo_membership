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
		assertThat(result.reservedRevenue()).isZero();
		assertThat(result.settledRevenue()).isZero();
	}

	@Test
	void returnsTotalAvailableAndSettledRevenueForCreatorScreen() {
		revenuePort.save(MembershipRevenue.empty(revenueUuid, creatorUuid).record(120_000L).settle(50_000L));

		RevenueResult result = service.get(new GetRevenueQuery(creatorUuid));

		assertThat(result.revenueUuid()).isEqualTo(revenueUuid);
		assertThat(result.totalRevenue()).isEqualTo(120_000L);
		assertThat(result.availableRevenue()).isEqualTo(70_000L);
		assertThat(result.reservedRevenue()).isZero();
		assertThat(result.settledRevenue()).isEqualTo(50_000L);
		assertThat(result.availableRevenue() + result.reservedRevenue() + result.settledRevenue())
				.isEqualTo(result.totalRevenue());
		assertThat(revenuePort.findByUuid(revenueUuid).orElseThrow().availableRevenue()).isEqualTo(70_000L);
	}

	@Test
	void doesNotExposeOtherCreatorRevenue() {
		revenuePort.save(MembershipRevenue.empty(revenueUuid, creatorUuid).record(120_000L).settle(50_000L));
		revenuePort.save(MembershipRevenue.empty(
				RevenueUuid.from("77777777-7777-7777-7777-777777777777"),
				CreatorUuid.from("33333333-3333-3333-3333-333333333333")
		).record(3_000L));

		RevenueResult result = service.get(new GetRevenueQuery(creatorUuid));

		assertThat(result.totalRevenue()).isEqualTo(120_000L);
		assertThat(result.availableRevenue()).isEqualTo(70_000L);
		assertThat(result.settledRevenue()).isEqualTo(50_000L);
	}
}
