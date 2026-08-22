package com.planwith.planwith_fo_membership.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_membership.adapter.out.grade.InMemoryGradeQueryAdapter;
import com.planwith.planwith_fo_membership.adapter.out.persistence.membership.InMemoryLoadMembershipPort;
import com.planwith.planwith_fo_membership.adapter.out.persistence.revenue.InMemoryRevenuePort;
import com.planwith.planwith_fo_membership.application.command.ValidateMembershipApplicationCommand;
import com.planwith.planwith_fo_membership.application.query.ApplyMembershipApplicationResult;
import com.planwith.planwith_fo_membership.application.query.MemberGradeResult;
import com.planwith.planwith_fo_membership.domain.exception.InsufficientMembershipGradeException;
import com.planwith.planwith_fo_membership.domain.model.MembershipRevenue;
import com.planwith.planwith_fo_membership.domain.model.MembershipStatus;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.RevenueUuid;
import com.planwith.planwith_fo_membership.domain.service.MembershipApplicationPolicy;

class ApplyMembershipApplicationServiceTest {

	private final CreatorUuid creatorUuid = CreatorUuid.from("22222222-2222-2222-2222-222222222222");
	private final MemberUuid memberUuid = new MemberUuid(creatorUuid.value());

	private InMemoryGradeQueryAdapter gradeQueryPort;
	private InMemoryLoadMembershipPort membershipPort;
	private InMemoryRevenuePort revenuePort;
	private ApplyMembershipApplicationService service;

	@BeforeEach
	void setUp() {
		gradeQueryPort = new InMemoryGradeQueryAdapter();
		membershipPort = new InMemoryLoadMembershipPort();
		revenuePort = new InMemoryRevenuePort();
		ValidateMembershipApplicationService validateService = new ValidateMembershipApplicationService(
				gradeQueryPort,
				membershipPort
		);
		service = new ApplyMembershipApplicationService(
				validateService,
				membershipPort,
				revenuePort,
				revenuePort
		);
	}

	@Test
	void applyCreatesPendingMembershipAndEmptyRevenueTogether() {
		gradeQueryPort.put(new MemberGradeResult(memberUuid, "EXPLORER", "탐험가", 4));

		ApplyMembershipApplicationResult result = service.apply(validCommand());

		assertThat(result.status()).isEqualTo(MembershipStatus.PENDING);
		assertThat(result.membershipName()).isEqualTo("윤휘명의 여행 멤버십");
		assertThat(result.monthlyPrice()).isEqualTo(100);
		assertThat(result.priceUnit()).isEqualTo(MembershipApplicationPolicy.PRICE_UNIT_TOKEN);
		assertThat(membershipPort.findByUuid(result.membershipUuid())).isPresent();
		assertThat(membershipPort.findByUuid(result.membershipUuid()).orElseThrow().status())
				.isEqualTo(MembershipStatus.PENDING);
		MembershipRevenue revenue = revenuePort.findByCreator(creatorUuid).orElseThrow();
		assertThat(revenue.revenueUuid()).isEqualTo(result.revenueUuid());
		assertThat(revenue.totalRevenue()).isZero();
		assertThat(revenue.availableRevenue()).isZero();
		assertThat(revenue.settledRevenue()).isZero();
	}

	@Test
	void travelerCannotApplyAndDoesNotCreateRevenue() {
		gradeQueryPort.put(new MemberGradeResult(memberUuid, "TRAVELER", "여행가", 3));

		assertThatThrownBy(() -> service.apply(validCommand()))
				.isInstanceOf(InsufficientMembershipGradeException.class);
		assertThat(membershipPort.findOpenByCreator(creatorUuid)).isEmpty();
		assertThat(revenuePort.findByCreator(creatorUuid)).isEmpty();
	}

	@Test
	void reusesExistingRevenueAccount() {
		gradeQueryPort.put(new MemberGradeResult(memberUuid, "EXPLORER", "탐험가", 4));
		RevenueUuid existingRevenueUuid = RevenueUuid.from("66666666-6666-6666-6666-666666666666");
		revenuePort.save(MembershipRevenue.empty(existingRevenueUuid, creatorUuid).record(100L));

		ApplyMembershipApplicationResult result = service.apply(validCommand());

		MembershipRevenue revenue = revenuePort.findByCreator(creatorUuid).orElseThrow();
		assertThat(result.revenueUuid()).isEqualTo(existingRevenueUuid);
		assertThat(revenue.totalRevenue()).isEqualTo(100L);
		assertThat(revenue.availableRevenue()).isEqualTo(100L);
	}

	private ValidateMembershipApplicationCommand validCommand() {
		return new ValidateMembershipApplicationCommand(
				creatorUuid,
				"윤휘명의 여행 멤버십",
				"월간 멤버십",
				100,
				"TOKEN"
		);
	}
}
