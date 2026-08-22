package com.planwith.planwith_fo_membership.domain.service;

import com.planwith.planwith_fo_membership.domain.model.MembershipSubscription;
import com.planwith.planwith_fo_membership.domain.model.SubscriptionStatus;

public final class EntitlementPolicy {

	private EntitlementPolicy() {
	}

	public static boolean canAccess(MembershipSubscription subscription) {
		return subscription != null && subscription.status() == SubscriptionStatus.ACTIVE;
	}
}
