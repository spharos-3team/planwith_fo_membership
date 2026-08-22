package com.planwith.planwith_fo_membership.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_membership.adapter.out.persistence.membership.InMemoryLoadMembershipPort;
import com.planwith.planwith_fo_membership.adapter.out.persistence.outbox.InMemoryMembershipEventOutboxPort;
import com.planwith.planwith_fo_membership.adapter.out.persistence.subscription.InMemoryLoadSubscriptionPort;
import com.planwith.planwith_fo_membership.adapter.out.redis.InMemoryEntitlementCacheAdapter;
import com.planwith.planwith_fo_membership.domain.event.MembershipEventTypes;
import com.planwith.planwith_fo_membership.domain.model.Membership;
import com.planwith.planwith_fo_membership.domain.model.MembershipSubscription;
import com.planwith.planwith_fo_membership.domain.model.SubscriptionStatus;
import com.planwith.planwith_fo_membership.domain.model.vo.AdminUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;

class ExpireDueSubscriptionsServiceTest {

	private final MemberUuid memberUuid = MemberUuid.from("11111111-1111-1111-1111-111111111111");
	private final CreatorUuid creatorUuid = CreatorUuid.from("22222222-2222-2222-2222-222222222222");
	private final MembershipUuid membershipUuid = MembershipUuid.from("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
	private final SubscriptionUuid dueUuid = SubscriptionUuid.from("55555555-5555-5555-5555-555555555555");
	private final SubscriptionUuid recentUuid = SubscriptionUuid.from("66666666-6666-6666-6666-666666666666");
	private final Instant now = Instant.parse("2026-08-22T00:00:00Z");

	private InMemoryLoadSubscriptionPort subscriptionPort;
	private InMemoryMembershipEventOutboxPort outboxPort;
	private ExpireDueSubscriptionsService expireDueSubscriptionsService;

	@BeforeEach
	void setUp() {
		InMemoryLoadMembershipPort membershipPort = new InMemoryLoadMembershipPort();
		subscriptionPort = new InMemoryLoadSubscriptionPort();
		outboxPort = new InMemoryMembershipEventOutboxPort();
		ExpireSubscriptionService expireSubscriptionService = new ExpireSubscriptionService(
				subscriptionPort,
				subscriptionPort,
				membershipPort,
				new RevokeEntitlementService(new InMemoryEntitlementCacheAdapter()),
				outboxPort,
				new ObjectMapper().findAndRegisterModules()
		);
		expireDueSubscriptionsService = new ExpireDueSubscriptionsService(subscriptionPort, expireSubscriptionService);
		Membership membership = Membership.apply(
				membershipUuid,
				creatorUuid,
				"윤휘명의 여행 멤버십",
				"월간 멤버십",
				100,
				Instant.parse("2026-07-01T00:00:00Z")
		).approve(AdminUuid.from("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"));
		membershipPort.save(membership);
		subscriptionPort.registerMembership(membership);
	}

	@Test
	void expiresSubscriptionsStartedThirtyOneDaysAgoAndKeepsRecentActive() {
		subscriptionPort.save(MembershipSubscription.active(
				dueUuid,
				membershipUuid,
				memberUuid,
				Instant.parse("2026-07-22T00:00:00Z")
		));
		subscriptionPort.save(MembershipSubscription.active(
				recentUuid,
				membershipUuid,
				MemberUuid.from("33333333-3333-3333-3333-333333333333"),
				Instant.parse("2026-08-20T00:00:00Z")
		));

		int expiredCount = expireDueSubscriptionsService.expireDue(now);

		assertThat(expiredCount).isEqualTo(1);
		assertThat(subscriptionPort.findByUuid(dueUuid).orElseThrow().status()).isEqualTo(SubscriptionStatus.INACTIVE);
		assertThat(subscriptionPort.findByUuid(dueUuid).orElseThrow().endedAt()).isEqualTo(now);
		assertThat(subscriptionPort.findByUuid(recentUuid).orElseThrow().status()).isEqualTo(SubscriptionStatus.ACTIVE);
		assertThat(outboxPort.messages()).hasSize(1);
		assertThat(outboxPort.messages().get(0).eventType()).isEqualTo(MembershipEventTypes.MEMBERSHIP_EXPIRED);
	}
}
