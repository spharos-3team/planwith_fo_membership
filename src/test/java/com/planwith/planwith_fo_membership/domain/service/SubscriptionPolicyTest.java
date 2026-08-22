package com.planwith.planwith_fo_membership.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_membership.domain.model.SubscriptionStatus;

class SubscriptionPolicyTest {

	@Test
	void onlyActiveSubscriptionCanBeDeactivated() {
		assertThat(SubscriptionPolicy.canDeactivate(SubscriptionStatus.ACTIVE)).isTrue();
		assertThat(SubscriptionPolicy.canDeactivate(SubscriptionStatus.INACTIVE)).isFalse();
		assertThat(SubscriptionPolicy.canDeactivate(null)).isFalse();
	}

	@Test
	void activeSubscriptionMustNotHaveEndedAt() {
		Instant startedAt = Instant.parse("2026-08-22T00:01:00Z");

		assertThat(SubscriptionPolicy.isValidPeriod(SubscriptionStatus.ACTIVE, startedAt, null)).isTrue();
		assertThat(SubscriptionPolicy.isValidPeriod(
				SubscriptionStatus.ACTIVE,
				startedAt,
				Instant.parse("2026-08-22T00:02:00Z")
		)).isFalse();
	}

	@Test
	void inactiveSubscriptionMustHaveEndedAtOnOrAfterStart() {
		Instant startedAt = Instant.parse("2026-08-22T00:01:00Z");

		assertThat(SubscriptionPolicy.isValidPeriod(
				SubscriptionStatus.INACTIVE,
				startedAt,
				Instant.parse("2026-08-22T00:02:00Z")
		)).isTrue();
		assertThat(SubscriptionPolicy.isValidPeriod(SubscriptionStatus.INACTIVE, startedAt, null)).isFalse();
		assertThat(SubscriptionPolicy.isValidPeriod(
				SubscriptionStatus.INACTIVE,
				startedAt,
				Instant.parse("2026-08-22T00:00:00Z")
		)).isFalse();
	}

	@Test
	void duplicateActiveIsDetected() {
		assertThat(SubscriptionPolicy.isDuplicateActive(true)).isTrue();
		assertThat(SubscriptionPolicy.isDuplicateActive(false)).isFalse();
	}

	@Test
	void isDueToExpireWhenTermHasElapsed() {
		Instant startedAt = Instant.parse("2026-07-23T00:00:00Z");
		Instant now = Instant.parse("2026-08-22T00:00:00Z");

		assertThat(SubscriptionPolicy.isDueToExpire(startedAt, now)).isTrue();
		assertThat(SubscriptionPolicy.isDueToExpire(startedAt, Instant.parse("2026-08-21T23:59:59Z"))).isFalse();
		assertThat(SubscriptionPolicy.isDueToExpire(null, now)).isFalse();
		assertThat(SubscriptionPolicy.isDueToExpire(startedAt, null)).isFalse();
	}
}
