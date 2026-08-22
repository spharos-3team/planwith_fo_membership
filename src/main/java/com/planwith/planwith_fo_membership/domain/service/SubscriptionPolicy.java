package com.planwith.planwith_fo_membership.domain.service;

import java.time.Duration;
import java.time.Instant;

import com.planwith.planwith_fo_membership.domain.model.SubscriptionStatus;

public final class SubscriptionPolicy {

	public static final Duration DEFAULT_TERM = Duration.ofDays(30);
	public static final String PROCESSED_BY_SYSTEM = "SYSTEM";

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

	public static boolean isDueToExpire(Instant startedAt, Instant now) {
		return startedAt != null && now != null && !now.isBefore(startedAt.plus(DEFAULT_TERM));
	}
}
