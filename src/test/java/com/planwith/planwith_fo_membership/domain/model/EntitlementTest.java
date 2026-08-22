package com.planwith.planwith_fo_membership.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_membership.domain.exception.InvalidSubscriptionStateException;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;
import com.planwith.planwith_fo_membership.domain.service.EntitlementPolicy;

class EntitlementTest {

	private final MemberUuid memberUuid = MemberUuid.from("11111111-1111-1111-1111-111111111111");
	private final CreatorUuid creatorUuid = CreatorUuid.from("22222222-2222-2222-2222-222222222222");
	private final MembershipSubscription active = MembershipSubscription.active(
			SubscriptionUuid.from("77777777-7777-7777-7777-777777777777"),
			MembershipUuid.from("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
			memberUuid,
			Instant.parse("2026-08-22T00:01:00Z")
	);

	@Test
	void grantFromActiveSubscription() {
		Entitlement entitlement = Entitlement.grant(active, creatorUuid);

		assertThat(entitlement.allowed()).isTrue();
		assertThat(entitlement.status()).isEqualTo(EntitlementPolicy.CACHE_VALUE_ACTIVE);
		assertThat(entitlement.subscriptionUuid()).isEqualTo(active.subscriptionUuid());
	}

	@Test
	void grantRejectsInactiveSubscription() {
		MembershipSubscription inactive = active.deactivate(Instant.parse("2026-08-22T00:02:00Z"));

		assertThatThrownBy(() -> Entitlement.grant(inactive, creatorUuid))
				.isInstanceOf(InvalidSubscriptionStateException.class);
		assertThat(Entitlement.from(inactive, creatorUuid).allowed()).isFalse();
	}

	@Test
	void cachedActiveAllowsAccessWithoutSubscriptionRow() {
		Entitlement entitlement = Entitlement.cachedActive(memberUuid, creatorUuid);

		assertThat(entitlement.allowed()).isTrue();
		assertThat(entitlement.status()).isEqualTo("ACTIVE");
		assertThat(Entitlement.denied(memberUuid, creatorUuid).allowed()).isFalse();
	}
}
