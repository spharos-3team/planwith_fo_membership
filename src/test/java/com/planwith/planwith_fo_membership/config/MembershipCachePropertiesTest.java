package com.planwith.planwith_fo_membership.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MembershipCachePropertiesTest {

	@Test
	void entitlementKeyUsesMemberAndCreator() {
		MembershipCacheProperties properties = new MembershipCacheProperties();

		assertThat(properties.entitlementKey(
				"11111111-1111-1111-1111-111111111111",
				"22222222-2222-2222-2222-222222222222"
		)).isEqualTo("membership:entitlement:11111111-1111-1111-1111-111111111111:22222222-2222-2222-2222-222222222222");
	}
}
