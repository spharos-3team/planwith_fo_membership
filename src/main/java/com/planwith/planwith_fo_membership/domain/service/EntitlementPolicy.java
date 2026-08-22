package com.planwith.planwith_fo_membership.domain.service;

import com.planwith.planwith_fo_membership.domain.model.MembershipSubscription;
import com.planwith.planwith_fo_membership.domain.model.SubscriptionStatus;

public final class EntitlementPolicy {

	public static final String CACHE_VALUE_ACTIVE = "ACTIVE";

	private EntitlementPolicy() {
	}

	public static boolean canAccess(MembershipSubscription subscription) {
		return canGrant(subscription);
	}

	public static boolean canGrant(MembershipSubscription subscription) {
		return subscription != null && subscription.status() == SubscriptionStatus.ACTIVE;
	}

	public static boolean isActiveCacheValue(String value) {
		return value != null && CACHE_VALUE_ACTIVE.equals(value.trim());
	}
}
