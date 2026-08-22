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
import com.planwith.planwith_fo_membership.application.command.ExpireSubscriptionCommand;
import com.planwith.planwith_fo_membership.domain.event.MembershipEventTypes;
import com.planwith.planwith_fo_membership.domain.model.Entitlement;
import com.planwith.planwith_fo_membership.domain.model.Membership;
import com.planwith.planwith_fo_membership.domain.model.MembershipSubscription;
import com.planwith.planwith_fo_membership.domain.model.SubscriptionStatus;
import com.planwith.planwith_fo_membership.domain.model.vo.AdminUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;
import com.planwith.planwith_fo_membership.domain.service.SubscriptionPolicy;

class ExpireSubscriptionServiceTest {

	private final MemberUuid memberUuid = MemberUuid.from("11111111-1111-1111-1111-111111111111");
	private final CreatorUuid creatorUuid = CreatorUuid.from("22222222-2222-2222-2222-222222222222");
	private final MembershipUuid membershipUuid = MembershipUuid.from("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
	private final SubscriptionUuid subscriptionUuid = SubscriptionUuid.from("55555555-5555-5555-5555-555555555555");
	private final Instant startedAt = Instant.parse("2026-07-22T00:01:00Z");
	private final Instant expiredAt = Instant.parse("2026-08-22T00:01:00Z");

	private InMemoryLoadSubscriptionPort subscriptionPort;
	private InMemoryEntitlementCacheAdapter entitlementCacheAdapter;
	private InMemoryMembershipEventOutboxPort outboxPort;
	private ExpireSubscriptionService expireSubscriptionService;

	@BeforeEach
	void setUp() {
		InMemoryLoadMembershipPort membershipPort = new InMemoryLoadMembershipPort();
		subscriptionPort = new InMemoryLoadSubscriptionPort();
		entitlementCacheAdapter = new InMemoryEntitlementCacheAdapter();
		outboxPort = new InMemoryMembershipEventOutboxPort();
		expireSubscriptionService = new ExpireSubscriptionService(
				subscriptionPort,
				subscriptionPort,
				membershipPort,
				new RevokeEntitlementService(entitlementCacheAdapter),
				outboxPort,
				new ObjectMapper().findAndRegisterModules()
		);
		Membership membership = Membership.apply(
				membershipUuid,
				creatorUuid,
				"윤휘명의 여행 멤버십",
				"월간 멤버십",
				100,
				Instant.parse("2026-07-22T00:00:00Z")
		).approve(AdminUuid.from("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"));
		membershipPort.save(membership);
		subscriptionPort.registerMembership(membership);
		subscriptionPort.save(MembershipSubscription.active(subscriptionUuid, membershipUuid, memberUuid, startedAt));
		entitlementCacheAdapter.save(Entitlement.cachedActive(memberUuid, creatorUuid));
	}

	@Test
	void expiresActiveSubscriptionAndRevokesEntitlement() {
		expireSubscriptionService.expire(new ExpireSubscriptionCommand(subscriptionUuid, expiredAt));

		MembershipSubscription expired = subscriptionPort.findByUuid(subscriptionUuid).orElseThrow();
		assertThat(expired.status()).isEqualTo(SubscriptionStatus.INACTIVE);
		assertThat(expired.endedAt()).isEqualTo(expiredAt);
		assertThat(entitlementCacheAdapter.find(memberUuid, creatorUuid)).isEmpty();
		assertThat(outboxPort.messages()).hasSize(1);
		assertThat(outboxPort.messages().get(0).eventType()).isEqualTo(MembershipEventTypes.MEMBERSHIP_EXPIRED);
		assertThat(outboxPort.messages().get(0).payload())
				.contains("\"processedBy\":\"" + SubscriptionPolicy.PROCESSED_BY_SYSTEM + "\"");
	}

	@Test
	void secondExpireIsIdempotentAndDoesNotDuplicateEvent() {
		ExpireSubscriptionCommand command = new ExpireSubscriptionCommand(subscriptionUuid, expiredAt);

		expireSubscriptionService.expire(command);
		expireSubscriptionService.expire(command);

		assertThat(subscriptionPort.findByUuid(subscriptionUuid).orElseThrow().status())
				.isEqualTo(SubscriptionStatus.INACTIVE);
		assertThat(outboxPort.messages()).hasSize(1);
		assertThat(outboxPort.messages().get(0).eventType()).isEqualTo(MembershipEventTypes.MEMBERSHIP_EXPIRED);
	}
}
