package com.planwith.planwith_fo_membership.domain.service;

import com.planwith.planwith_fo_membership.domain.model.Membership;

public final class JoinEligibilityPolicy {

	private JoinEligibilityPolicy() {
	}

	public static boolean canAcceptJoin(Membership membership) {
		return MembershipPolicy.canAcceptSubscription(membership);
	}

	public static boolean requiresFollow(boolean following) {
		return !following;
	}

	public static boolean isDuplicateSubscription(boolean hasActiveSubscription) {
		return hasActiveSubscription;
	}
}
