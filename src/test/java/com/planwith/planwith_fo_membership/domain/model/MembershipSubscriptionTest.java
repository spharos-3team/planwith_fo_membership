package com.planwith.planwith_fo_membership.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_membership.domain.exception.InvalidSubscriptionStateException;
import com.planwith.planwith_fo_membership.domain.model.vo.AdminUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;
import com.planwith.planwith_fo_membership.domain.service.EntitlementPolicy;

class MembershipSubscriptionTest {

	private final MemberUuid memberUuid = new MemberUuid(UUID.fromString("11111111-1111-1111-1111-111111111111"));
	private final MembershipUuid membershipUuid = new MembershipUuid(UUID.fromString("99999999-9999-9999-9999-999999999999"));
	private final SubscriptionUuid subscriptionUuid = new SubscriptionUuid(UUID.fromString("33333333-3333-3333-3333-333333333333"));

	@Test
	void subscribeFromApprovedMembershipThenDeactivate() {
		Membership approved = Membership.apply(
						membershipUuid,
						new CreatorUuid(UUID.fromString("22222222-2222-2222-2222-222222222222")),
						"크리에이터 멤버십",
						null,
						9900,
						Instant.parse("2026-08-22T00:00:00Z")
				)
				.approve(new AdminUuid(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")));

		MembershipSubscription active = MembershipSubscription.subscribe(
				approved,
				subscriptionUuid,
				memberUuid,
				Instant.parse("2026-08-22T00:01:00Z")
		);
		MembershipSubscription inactive = active.deactivate(Instant.parse("2026-08-22T00:02:00Z"));

		assertThat(active.status()).isEqualTo(SubscriptionStatus.ACTIVE);
		assertThat(EntitlementPolicy.canAccess(active)).isTrue();
		assertThat(inactive.status()).isEqualTo(SubscriptionStatus.INACTIVE);
		assertThat(EntitlementPolicy.canAccess(inactive)).isFalse();
	}

	@Test
	void deactivateRejectsInactiveSubscription() {
		MembershipSubscription inactive = MembershipSubscription.active(
						subscriptionUuid,
						membershipUuid,
						memberUuid,
						Instant.parse("2026-08-22T00:00:00Z")
				)
				.deactivate(Instant.parse("2026-08-22T00:02:00Z"));

		assertThatThrownBy(() -> inactive.deactivate(Instant.parse("2026-08-22T00:03:00Z")))
				.isInstanceOf(InvalidSubscriptionStateException.class);
	}

	@Test
	void restoreRejectsInvalidPeriod() {
		assertThatThrownBy(() -> MembershipSubscription.restore(
				subscriptionUuid,
				membershipUuid,
				memberUuid,
				SubscriptionStatus.ACTIVE,
				Instant.parse("2026-08-22T00:00:00Z"),
				Instant.parse("2026-08-22T00:02:00Z")
		)).isInstanceOf(InvalidSubscriptionStateException.class);
	}
}
