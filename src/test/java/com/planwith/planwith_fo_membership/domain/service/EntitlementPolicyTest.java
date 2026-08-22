package com.planwith.planwith_fo_membership.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_membership.domain.model.MembershipSubscription;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;

class EntitlementPolicyTest {

	private final MembershipSubscription active = MembershipSubscription.active(
			SubscriptionUuid.from("77777777-7777-7777-7777-777777777777"),
			MembershipUuid.from("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
			MemberUuid.from("11111111-1111-1111-1111-111111111111"),
			Instant.parse("2026-08-22T00:01:00Z")
	);

	@Test
	void onlyActiveSubscriptionCanBeGrantedAndAccessed() {
		MembershipSubscription inactive = active.deactivate(Instant.parse("2026-08-22T00:02:00Z"));

		assertThat(EntitlementPolicy.canGrant(active)).isTrue();
		assertThat(EntitlementPolicy.canAccess(active)).isTrue();
		assertThat(EntitlementPolicy.canGrant(inactive)).isFalse();
		assertThat(EntitlementPolicy.canAccess(inactive)).isFalse();
		assertThat(EntitlementPolicy.canGrant(null)).isFalse();
	}

	@Test
	void redisCacheValueIsActive() {
		assertThat(EntitlementPolicy.isActiveCacheValue("ACTIVE")).isTrue();
		assertThat(EntitlementPolicy.isActiveCacheValue(" ACTIVE ")).isTrue();
		assertThat(EntitlementPolicy.isActiveCacheValue("{\"allowed\":true}")).isFalse();
		assertThat(EntitlementPolicy.isActiveCacheValue(null)).isFalse();
		assertThat(EntitlementPolicy.isActiveCacheValue("")).isFalse();
	}
}
