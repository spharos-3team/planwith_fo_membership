package com.planwith.planwith_fo_membership.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_membership.application.port.in.query.GetRevenueQueryUseCase;
import com.planwith.planwith_fo_membership.application.port.out.LoadRevenuePort;
import com.planwith.planwith_fo_membership.application.port.out.SaveRevenuePort;
import com.planwith.planwith_fo_membership.application.query.GetRevenueQuery;
import com.planwith.planwith_fo_membership.application.query.RevenueResult;
import com.planwith.planwith_fo_membership.domain.model.MembershipRevenue;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.RevenueUuid;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class GetRevenueQueryServiceIntegrationTest {

	private final CreatorUuid creatorUuid = CreatorUuid.from("22222222-2222-2222-2222-222222222222");
	private final RevenueUuid revenueUuid = RevenueUuid.from("66666666-6666-6666-6666-666666666666");

	@Autowired
	private GetRevenueQueryUseCase getRevenueQueryUseCase;

	@Autowired
	private SaveRevenuePort saveRevenuePort;

	@Autowired
	private LoadRevenuePort loadRevenuePort;

	@Test
	void returnsCreatorScreenAmountsWithoutMutatingRevenue() {
		saveRevenuePort.save(MembershipRevenue.empty(revenueUuid, creatorUuid).record(120_000L).settle(50_000L));

		RevenueResult result = getRevenueQueryUseCase.get(new GetRevenueQuery(creatorUuid));

		assertThat(result.revenueUuid()).isEqualTo(revenueUuid);
		assertThat(result.totalRevenue()).isEqualTo(120_000L);
		assertThat(result.availableRevenue()).isEqualTo(70_000L);
		assertThat(result.settledRevenue()).isEqualTo(50_000L);

		MembershipRevenue stored = loadRevenuePort.findByUuid(revenueUuid).orElseThrow();
		assertThat(stored.totalRevenue()).isEqualTo(120_000L);
		assertThat(stored.availableRevenue()).isEqualTo(70_000L);
		assertThat(stored.settledRevenue()).isEqualTo(50_000L);
	}

	@Test
	void returnsZerosWhenCreatorHasNoRevenueAccount() {
		RevenueResult result = getRevenueQueryUseCase.get(new GetRevenueQuery(creatorUuid));

		assertThat(result.revenueUuid()).isNull();
		assertThat(result.totalRevenue()).isZero();
		assertThat(result.availableRevenue()).isZero();
		assertThat(result.settledRevenue()).isZero();
	}
}
