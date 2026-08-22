package com.planwith.planwith_fo_membership.adapter.out.persistence.revenue;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_membership.application.port.out.LoadRevenuePort;
import com.planwith.planwith_fo_membership.application.port.out.SaveRevenuePort;
import com.planwith.planwith_fo_membership.domain.model.MembershipRevenue;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.RevenueUuid;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MembershipRevenuePersistenceAdapterIntegrationTest {

	@Autowired
	private SaveRevenuePort saveRevenuePort;

	@Autowired
	private LoadRevenuePort loadRevenuePort;

	@Test
	void saveAndLoadRevenueByCreator() {
		RevenueUuid revenueUuid = RevenueUuid.from("66666666-6666-6666-6666-666666666666");
		CreatorUuid creatorUuid = CreatorUuid.from("22222222-2222-2222-2222-222222222222");

		MembershipRevenue recorded = MembershipRevenue.empty(revenueUuid, creatorUuid)
				.record(12900L)
				.settle(2900L);
		saveRevenuePort.save(recorded);

		MembershipRevenue loaded = loadRevenuePort.findByCreator(creatorUuid).orElseThrow();
		assertThat(loaded.revenueUuid()).isEqualTo(revenueUuid);
		assertThat(loaded.totalRevenue()).isEqualTo(12900L);
		assertThat(loaded.availableRevenue()).isEqualTo(10000L);
		assertThat(loaded.reservedRevenue()).isZero();
		assertThat(loaded.settledRevenue()).isEqualTo(2900L);
		assertThat(loadRevenuePort.findByUuid(revenueUuid))
				.get()
				.extracting(MembershipRevenue::totalRevenue)
				.isEqualTo(12900L);
	}
}
