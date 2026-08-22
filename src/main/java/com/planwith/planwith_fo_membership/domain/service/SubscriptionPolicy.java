package com.planwith.planwith_fo_membership.domain.service;

import java.time.Instant;

import com.planwith.planwith_fo_membership.domain.model.SubscriptionStatus;

public final class SubscriptionPolicy {

	private SubscriptionPolicy() {
	}

	public static boolean canDeactivate(SubscriptionStatus status) {
		return status == SubscriptionStatus.ACTIVE;
	}

	public static boolean isValidPeriod(SubscriptionStatus status, Instant startedAt, Instant endedAt) {
		if (status == null || startedAt == null) {
			return false;
		}
		if (status == SubscriptionStatus.ACTIVE) {
			return endedAt == null;
		}
		if (status == SubscriptionStatus.INACTIVE) {
			return endedAt != null && !endedAt.isBefore(startedAt);
		}
		return false;
	}

	public static boolean isDuplicateActive(boolean hasOtherActiveSubscription) {
		return hasOtherActiveSubscription;
	}
}
