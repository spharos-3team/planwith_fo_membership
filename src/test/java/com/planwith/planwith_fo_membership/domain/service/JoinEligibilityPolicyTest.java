package com.planwith.planwith_fo_membership.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_membership.domain.model.Membership;
import com.planwith.planwith_fo_membership.domain.model.vo.AdminUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;

class JoinEligibilityPolicyTest {

	@Test
	void onlyApprovedMembershipCanAcceptJoin() {
		Membership pending = Membership.apply(
				new MembershipUuid(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")),
				new CreatorUuid(UUID.fromString("22222222-2222-2222-2222-222222222222")),
				"크리에이터 멤버십",
				"설명",
				100,
				Instant.parse("2026-08-22T00:00:00Z")
		);

		assertThat(JoinEligibilityPolicy.canAcceptJoin(pending)).isFalse();
		assertThat(JoinEligibilityPolicy.canAcceptJoin(
				pending.approve(AdminUuid.from("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"))
		)).isTrue();
		assertThat(JoinEligibilityPolicy.canAcceptJoin(null)).isFalse();
	}

	@Test
	void followIsRequiredBeforeJoin() {
		assertThat(JoinEligibilityPolicy.requiresFollow(false)).isTrue();
		assertThat(JoinEligibilityPolicy.requiresFollow(true)).isFalse();
	}

	@Test
	void activeSubscriptionIsDuplicate() {
		assertThat(JoinEligibilityPolicy.isDuplicateSubscription(true)).isTrue();
		assertThat(JoinEligibilityPolicy.isDuplicateSubscription(false)).isFalse();
	}
}
