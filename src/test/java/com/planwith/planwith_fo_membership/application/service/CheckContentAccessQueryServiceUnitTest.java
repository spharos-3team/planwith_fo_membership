package com.planwith.planwith_fo_membership.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_membership.adapter.out.persistence.subscription.InMemoryLoadSubscriptionPort;
import com.planwith.planwith_fo_membership.adapter.out.redis.InMemoryEntitlementCacheAdapter;
import com.planwith.planwith_fo_membership.application.query.CheckContentAccessQuery;
import com.planwith.planwith_fo_membership.application.query.ContentAccessResult;
import com.planwith.planwith_fo_membership.domain.model.Entitlement;
import com.planwith.planwith_fo_membership.domain.model.Membership;
import com.planwith.planwith_fo_membership.domain.model.MembershipSubscription;
import com.planwith.planwith_fo_membership.domain.model.vo.AdminUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;

class CheckContentAccessQueryServiceUnitTest {

	private final MemberUuid memberUuid = MemberUuid.from("11111111-1111-1111-1111-111111111111");
	private final CreatorUuid creatorUuid = CreatorUuid.from("22222222-2222-2222-2222-222222222222");
	private final MembershipUuid membershipUuid = MembershipUuid.from("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

	private InMemoryLoadSubscriptionPort subscriptionPort;
	private InMemoryEntitlementCacheAdapter entitlementCacheAdapter;
	private CheckContentAccessQueryService queryService;

	@BeforeEach
	void setUp() {
		subscriptionPort = new InMemoryLoadSubscriptionPort();
		entitlementCacheAdapter = new InMemoryEntitlementCacheAdapter();
		queryService = new CheckContentAccessQueryService(entitlementCacheAdapter, subscriptionPort);
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
	void cacheHitAllowsAccessWithoutSubscriptionLookup() {
		entitlementCacheAdapter.save(Entitlement.cachedActive(memberUuid, creatorUuid));

		ContentAccessResult result = queryService.check(new CheckContentAccessQuery(memberUuid, creatorUuid));

		assertThat(result.allowed()).isTrue();
		assertThat(result.status()).isEqualTo("ACTIVE");
		assertThat(subscriptionPort.findCurrentByMemberAndCreator(memberUuid, creatorUuid)).isEmpty();
	}

	@Test
	void cacheMissFallsBackToActiveSubscriptionAndWarmsCache() {
		subscriptionPort.save(MembershipSubscription.active(
				SubscriptionUuid.from("77777777-7777-7777-7777-777777777777"),
				membershipUuid,
				memberUuid,
				Instant.parse("2026-08-22T00:01:00Z")
		));

		ContentAccessResult result = queryService.check(new CheckContentAccessQuery(memberUuid, creatorUuid));

		assertThat(result.allowed()).isTrue();
		assertThat(result.status()).isEqualTo("ACTIVE");
		assertThat(entitlementCacheAdapter.find(memberUuid, creatorUuid).orElseThrow().allowed()).isTrue();
	}

	@Test
	void cacheFailureEquivalentMissDeniesWhenSubscriptionMissing() {
		ContentAccessResult result = queryService.check(new CheckContentAccessQuery(memberUuid, creatorUuid));

		assertThat(result.allowed()).isFalse();
		assertThat(result.status()).isNull();
		assertThat(entitlementCacheAdapter.find(memberUuid, creatorUuid)).isEmpty();
	}
}
