package com.planwith.planwith_fo_membership.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_membership.adapter.out.grade.InMemoryGradeQueryAdapter;
import com.planwith.planwith_fo_membership.application.command.ValidateMembershipApplicationCommand;
import com.planwith.planwith_fo_membership.application.port.in.command.ApplyMembershipApplicationUseCase;
import com.planwith.planwith_fo_membership.application.port.out.LoadMembershipPort;
import com.planwith.planwith_fo_membership.application.port.out.LoadRevenuePort;
import com.planwith.planwith_fo_membership.application.query.ApplyMembershipApplicationResult;
import com.planwith.planwith_fo_membership.application.query.MemberGradeResult;
import com.planwith.planwith_fo_membership.domain.model.Membership;
import com.planwith.planwith_fo_membership.domain.model.MembershipRevenue;
import com.planwith.planwith_fo_membership.domain.model.MembershipStatus;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(ApplyMembershipApplicationServiceIntegrationTest.GradeQueryTestConfig.class)
class ApplyMembershipApplicationServiceIntegrationTest {

	@Autowired
	private ApplyMembershipApplicationUseCase applyMembershipApplicationUseCase;

	@Autowired
	private LoadMembershipPort loadMembershipPort;

	@Autowired
	private LoadRevenuePort loadRevenuePort;

	@Autowired
	private InMemoryGradeQueryAdapter gradeQueryPort;

	private final CreatorUuid creatorUuid = CreatorUuid.from("22222222-2222-2222-2222-222222222222");

	@BeforeEach
	void setUp() {
		gradeQueryPort.put(new MemberGradeResult(
				new MemberUuid(creatorUuid.value()),
				"EXPLORER",
				"탐험가",
				4
		));
	}

	@Test
	void applyPersistsPendingMembershipAndEmptyRevenueInOneTransaction() {
		ApplyMembershipApplicationResult result = applyMembershipApplicationUseCase.apply(
				new ValidateMembershipApplicationCommand(
						creatorUuid,
						"윤휘명의 여행 멤버십",
						"월간 멤버십",
						100,
						"TOKEN"
				)
		);

		Membership membership = loadMembershipPort.findByUuid(result.membershipUuid()).orElseThrow();
		MembershipRevenue revenue = loadRevenuePort.findByCreator(creatorUuid).orElseThrow();

		assertThat(membership.status()).isEqualTo(MembershipStatus.PENDING);
		assertThat(membership.membershipName()).isEqualTo("윤휘명의 여행 멤버십");
		assertThat(membership.monthlyPrice()).isEqualTo(100);
		assertThat(membership.creatorUuid()).isEqualTo(creatorUuid);
		assertThat(revenue.revenueUuid()).isEqualTo(result.revenueUuid());
		assertThat(revenue.totalRevenue()).isZero();
		assertThat(revenue.availableRevenue()).isZero();
		assertThat(revenue.settledRevenue()).isZero();
	}

	@TestConfiguration
	static class GradeQueryTestConfig {

		@Bean
		@Primary
		InMemoryGradeQueryAdapter inMemoryGradeQueryAdapter() {
			return new InMemoryGradeQueryAdapter();
		}
	}
}
