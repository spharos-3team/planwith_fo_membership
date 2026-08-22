package com.planwith.planwith_fo_membership.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_membership.adapter.out.follow.InMemoryFollowQueryAdapter;
import com.planwith.planwith_fo_membership.adapter.out.persistence.membership.InMemoryLoadMembershipPort;
import com.planwith.planwith_fo_membership.adapter.out.persistence.subscription.InMemoryLoadSubscriptionPort;
import com.planwith.planwith_fo_membership.application.command.ValidateJoinEligibilityCommand;
import com.planwith.planwith_fo_membership.application.query.ValidateJoinEligibilityResult;
import com.planwith.planwith_fo_membership.domain.exception.DuplicateSubscriptionException;
import com.planwith.planwith_fo_membership.domain.exception.FollowRequiredException;
import com.planwith.planwith_fo_membership.domain.exception.InvalidMembershipStateException;
import com.planwith.planwith_fo_membership.domain.exception.MembershipNotFoundException;
import com.planwith.planwith_fo_membership.domain.model.Membership;
import com.planwith.planwith_fo_membership.domain.model.MembershipSubscription;
import com.planwith.planwith_fo_membership.domain.model.vo.AdminUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;
import com.planwith.planwith_fo_membership.domain.service.MembershipApplicationPolicy;

class ValidateJoinEligibilityServiceTest {

	private final MemberUuid memberUuid = MemberUuid.from("11111111-1111-1111-1111-111111111111");
	private final CreatorUuid creatorUuid = CreatorUuid.from("22222222-2222-2222-2222-222222222222");
	private final MembershipUuid membershipUuid = MembershipUuid.from("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
	private final AdminUuid adminUuid = AdminUuid.from("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

	private InMemoryLoadMembershipPort membershipPort;
	private InMemoryFollowQueryAdapter followQueryPort;
	private InMemoryLoadSubscriptionPort subscriptionPort;
	private ValidateJoinEligibilityService service;

	@BeforeEach
	void setUp() {
		membershipPort = new InMemoryLoadMembershipPort();
		followQueryPort = new InMemoryFollowQueryAdapter();
		subscriptionPort = new InMemoryLoadSubscriptionPort();
		service = new ValidateJoinEligibilityService(membershipPort, followQueryPort, subscriptionPort);
	}

	@Test
	void followerCanJoinApprovedMembershipWithoutActiveSubscription() {
		saveApprovedMembership();
		followQueryPort.follow(memberUuid, creatorUuid);

		ValidateJoinEligibilityResult result = service.validate(command());

		assertThat(result.eligible()).isTrue();
		assertThat(result.following()).isTrue();
		assertThat(result.membershipUuid()).isEqualTo(membershipUuid);
		assertThat(result.monthlyPrice()).isEqualTo(100);
		assertThat(result.priceUnit()).isEqualTo(MembershipApplicationPolicy.PRICE_UNIT_TOKEN);
	}

	@Test
	void rejectsWhenMembershipDoesNotExist() {
		followQueryPort.follow(memberUuid, creatorUuid);

		assertThatThrownBy(() -> service.validate(command()))
				.isInstanceOf(MembershipNotFoundException.class);
	}

	@Test
	void pendingMembershipIsNotJoinTarget() {
		membershipPort.save(pendingMembership());
		followQueryPort.follow(memberUuid, creatorUuid);

		assertThatThrownBy(() -> service.validate(command()))
				.isInstanceOf(InvalidMembershipStateException.class)
				.hasMessage("승인된 멤버십만 가입할 수 있습니다.");
	}

	@Test
	void nonFollowerGetsFollowRequiredBeforeTokenDeduction() {
		saveApprovedMembership();

		assertThatThrownBy(() -> service.validate(command()))
				.isInstanceOf(FollowRequiredException.class)
				.hasMessage("팔로워만 멤버십에 가입할 수 있습니다.");
	}

	@Test
	void rejectsDuplicateActiveSubscription() {
		Membership membership = saveApprovedMembership();
		followQueryPort.follow(memberUuid, creatorUuid);
		subscriptionPort.registerMembership(membership);
		subscriptionPort.save(MembershipSubscription.active(
				SubscriptionUuid.from("55555555-5555-5555-5555-555555555555"),
				membershipUuid,
				memberUuid,
				Instant.parse("2026-08-22T00:01:00Z")
		));

		assertThatThrownBy(() -> service.validate(command()))
				.isInstanceOf(DuplicateSubscriptionException.class)
				.hasMessage("이미 가입한 멤버십입니다.");
	}

	@Test
	void inactiveSubscriptionDoesNotBlockJoin() {
		Membership membership = saveApprovedMembership();
		followQueryPort.follow(memberUuid, creatorUuid);
		subscriptionPort.registerMembership(membership);
		subscriptionPort.save(MembershipSubscription.active(
				SubscriptionUuid.from("55555555-5555-5555-5555-555555555555"),
				membershipUuid,
				memberUuid,
				Instant.parse("2026-08-22T00:01:00Z")
		).deactivate(Instant.parse("2026-08-22T00:02:00Z")));

		ValidateJoinEligibilityResult result = service.validate(command());

		assertThat(result.eligible()).isTrue();
	}

	private ValidateJoinEligibilityCommand command() {
		return new ValidateJoinEligibilityCommand(memberUuid, creatorUuid);
	}

	private Membership pendingMembership() {
		return Membership.apply(
				membershipUuid,
				creatorUuid,
				"윤휘명의 여행 멤버십",
				"월간 멤버십",
				100,
				Instant.parse("2026-08-22T00:00:00Z")
		);
	}

	private Membership saveApprovedMembership() {
		Membership membership = pendingMembership().approve(adminUuid);
		membershipPort.save(membership);
		return membership;
	}
}
