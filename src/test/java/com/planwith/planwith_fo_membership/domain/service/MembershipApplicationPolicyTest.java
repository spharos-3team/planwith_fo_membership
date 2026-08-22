package com.planwith.planwith_fo_membership.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_membership.domain.model.Membership;
import com.planwith.planwith_fo_membership.domain.model.vo.AdminUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;

class MembershipApplicationPolicyTest {

	@Test
	void explorerAndAboveCanOpenMembership() {
		assertThat(MembershipApplicationPolicy.canOpenMembership("ROOKIE", 1)).isFalse();
		assertThat(MembershipApplicationPolicy.canOpenMembership("LEAF", 2)).isFalse();
		assertThat(MembershipApplicationPolicy.canOpenMembership("TRAVELER", 3)).isFalse();
		assertThat(MembershipApplicationPolicy.canOpenMembership("EXPLORER", 4)).isTrue();
		assertThat(MembershipApplicationPolicy.canOpenMembership("ADVENTURE", 5)).isTrue();
		assertThat(MembershipApplicationPolicy.canOpenMembership("PLANWITH", 6)).isTrue();
	}

	@Test
	void priceMustBePositiveToken() {
		assertThat(MembershipApplicationPolicy.isPositivePrice(1)).isTrue();
		assertThat(MembershipApplicationPolicy.isPositivePrice(0)).isFalse();
		assertThat(MembershipApplicationPolicy.isTokenPriceUnit("TOKEN")).isTrue();
		assertThat(MembershipApplicationPolicy.isTokenPriceUnit("KRW")).isFalse();
	}

	@Test
	void pendingOrApprovedMembershipIsDuplicate() {
		Membership pending = Membership.apply(
				new MembershipUuid(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")),
				new CreatorUuid(UUID.fromString("22222222-2222-2222-2222-222222222222")),
				"크리에이터 멤버십",
				"설명",
				12900,
				Instant.parse("2026-08-22T00:00:00Z")
		);
		Membership approved = pending.approve(AdminUuid.from("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"));
		Membership rejected = pending.reject(AdminUuid.from("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"), "거절");

		assertThat(MembershipApplicationPolicy.isDuplicateApplication(pending)).isTrue();
		assertThat(MembershipApplicationPolicy.isDuplicateApplication(approved)).isTrue();
		assertThat(MembershipApplicationPolicy.isDuplicateApplication(rejected)).isFalse();
		assertThat(MembershipApplicationPolicy.isDuplicateApplication(null)).isFalse();
	}
}
