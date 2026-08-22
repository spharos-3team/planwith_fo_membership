package com.planwith.planwith_fo_membership.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_membership.domain.exception.InvalidMembershipStateException;
import com.planwith.planwith_fo_membership.domain.exception.InvalidSubscriptionStateException;
import com.planwith.planwith_fo_membership.domain.model.vo.AdminUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;
import com.planwith.planwith_fo_membership.domain.service.MembershipPolicy;

class MembershipTest {

	private final MembershipUuid membershipUuid = new MembershipUuid(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
	private final CreatorUuid creatorUuid = new CreatorUuid(UUID.fromString("22222222-2222-2222-2222-222222222222"));
	private final AdminUuid adminUuid = new AdminUuid(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"));

	@Test
	void applyCreatesPendingMembershipWithoutAdmin() {
		Membership applied = Membership.apply(
				membershipUuid,
				creatorUuid,
				"크리에이터 멤버십",
				"월간 멤버십",
				12900,
				Instant.parse("2026-08-22T00:00:00Z")
		);

		assertThat(applied.status()).isEqualTo(MembershipStatus.PENDING);
		assertThat(applied.adminUuid()).isNull();
		assertThat(applied.isApproved()).isFalse();
		assertThat(MembershipPolicy.canAcceptSubscription(applied)).isFalse();
	}

	@Test
	void approveAndDeactivateFollowsStatusPolicy() {
		Membership approved = pending().approve(adminUuid);
		Membership inactive = approved.deactivate();

		assertThat(approved.status()).isEqualTo(MembershipStatus.APPROVED);
		assertThat(approved.adminUuid()).isEqualTo(adminUuid);
		assertThat(MembershipPolicy.canAcceptSubscription(approved)).isTrue();
		assertThat(inactive.status()).isEqualTo(MembershipStatus.INACTIVE);
		assertThat(MembershipPolicy.canAcceptSubscription(inactive)).isFalse();
	}

	@Test
	void rejectPendingMembership() {
		Membership rejected = pending().reject(adminUuid, "서류 미비");

		assertThat(rejected.status()).isEqualTo(MembershipStatus.REJECTED);
		assertThat(rejected.rejectReason()).isEqualTo("서류 미비");
		assertThat(MembershipPolicy.canAcceptSubscription(rejected)).isFalse();
	}

	@Test
	void rejectApprovedMembershipFails() {
		assertThatThrownBy(() -> pending().approve(adminUuid).reject(adminUuid, "불가"))
				.isInstanceOf(InvalidMembershipStateException.class);
	}

	@Test
	void subscribeRequiresApprovedMembership() {
		assertThatThrownBy(() -> MembershipSubscription.subscribe(
				pending(),
				new SubscriptionUuid(UUID.fromString("33333333-3333-3333-3333-333333333333")),
				new MemberUuid(UUID.fromString("11111111-1111-1111-1111-111111111111")),
				Instant.parse("2026-08-22T00:01:00Z")
		)).isInstanceOf(InvalidSubscriptionStateException.class);
	}

	private Membership pending() {
		return Membership.apply(
				membershipUuid,
				creatorUuid,
				"크리에이터 멤버십",
				"월간 멤버십",
				12900,
				Instant.parse("2026-08-22T00:00:00Z")
		);
	}
}
