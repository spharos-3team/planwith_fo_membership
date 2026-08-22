package com.planwith.planwith_fo_membership.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_membership.adapter.out.persistence.subscription.InMemoryLoadSubscriptionPort;
import com.planwith.planwith_fo_membership.adapter.out.redis.InMemoryEntitlementCacheAdapter;
import com.planwith.planwith_fo_membership.application.command.GrantEntitlementCommand;
import com.planwith.planwith_fo_membership.domain.model.Membership;
import com.planwith.planwith_fo_membership.domain.model.MembershipSubscription;
import com.planwith.planwith_fo_membership.domain.model.vo.AdminUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;

class GrantEntitlementServiceTest {

	private final MemberUuid memberUuid = MemberUuid.from("11111111-1111-1111-1111-111111111111");
	private final CreatorUuid creatorUuid = CreatorUuid.from("22222222-2222-2222-2222-222222222222");
	private final MembershipUuid membershipUuid = MembershipUuid.from("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
	private final SubscriptionUuid subscriptionUuid = SubscriptionUuid.from("77777777-7777-7777-7777-777777777777");

	private InMemoryLoadSubscriptionPort subscriptionPort;
	private InMemoryEntitlementCacheAdapter entitlementCacheAdapter;
	private GrantEntitlementService grantEntitlementService;

	@BeforeEach
	void setUp() {
		subscriptionPort = new InMemoryLoadSubscriptionPort();
		entitlementCacheAdapter = new InMemoryEntitlementCacheAdapter();
		grantEntitlementService = new GrantEntitlementService(subscriptionPort, entitlementCacheAdapter);
		Membership membership = Membership.apply(
				membershipUuid,
				creatorUuid,
				"윤휘명의 여행 멤버십",
				"월간 멤버십",
				100,
				Instant.parse("2026-08-22T00:00:00Z")
		).approve(AdminUuid.from("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"));
		subscriptionPort.registerMembership(membership);
	}

	@Test
	void grantsActiveCacheOnceForSameSubscription() {
		subscriptionPort.save(MembershipSubscription.active(
				subscriptionUuid,
				membershipUuid,
				memberUuid,
				Instant.parse("2026-08-22T00:01:00Z")
		));
		GrantEntitlementCommand command = new GrantEntitlementCommand(memberUuid, creatorUuid, subscriptionUuid);

		grantEntitlementService.grant(command);
		grantEntitlementService.grant(command);

		assertThat(entitlementCacheAdapter.find(memberUuid, creatorUuid).orElseThrow().allowed()).isTrue();
		assertThat(entitlementCacheAdapter.find(memberUuid, creatorUuid).orElseThrow().status()).isEqualTo("ACTIVE");
	}

	@Test
	void doesNotGrantWhenSubscriptionIsInactive() {
		subscriptionPort.save(MembershipSubscription.active(
				subscriptionUuid,
				membershipUuid,
				memberUuid,
				Instant.parse("2026-08-22T00:01:00Z")
		).deactivate(Instant.parse("2026-08-22T00:02:00Z")));

		grantEntitlementService.grant(new GrantEntitlementCommand(memberUuid, creatorUuid, subscriptionUuid));

		assertThat(entitlementCacheAdapter.find(memberUuid, creatorUuid)).isEmpty();
	}

	@Test
	void doesNotGrantWhenSubscriptionIsMissing() {
		grantEntitlementService.grant(new GrantEntitlementCommand(memberUuid, creatorUuid, subscriptionUuid));

		assertThat(entitlementCacheAdapter.find(memberUuid, creatorUuid)).isEmpty();
	}
}
