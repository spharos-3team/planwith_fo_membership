package com.planwith.planwith_fo_membership.domain.service;

import com.planwith.planwith_fo_membership.domain.model.Membership;

public final class MembershipPolicy {

	private MembershipPolicy() {
	}

	public static boolean canAcceptSubscription(Membership membership) {
		return membership != null && membership.isApproved();
	}
}
