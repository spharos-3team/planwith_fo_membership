package com.planwith.planwith_fo_membership.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_membership.adapter.out.persistence.membership.InMemoryLoadMembershipPort;
import com.planwith.planwith_fo_membership.adapter.out.persistence.outbox.InMemoryMembershipEventOutboxPort;
import com.planwith.planwith_fo_membership.adapter.out.persistence.payment.InMemoryPaymentPort;
import com.planwith.planwith_fo_membership.adapter.out.persistence.subscription.InMemoryLoadSubscriptionPort;
import com.planwith.planwith_fo_membership.adapter.out.redis.InMemoryEntitlementCacheAdapter;
import com.planwith.planwith_fo_membership.application.command.CancelSubscriptionCommand;
import com.planwith.planwith_fo_membership.application.query.CancelSubscriptionResult;
import com.planwith.planwith_fo_membership.application.query.CheckContentAccessQuery;
import com.planwith.planwith_fo_membership.application.query.ContentAccessResult;
import com.planwith.planwith_fo_membership.domain.event.MembershipEventTypes;
import com.planwith.planwith_fo_membership.domain.exception.ForbiddenCreatorException;
import com.planwith.planwith_fo_membership.domain.exception.InvalidSubscriptionStateException;
import com.planwith.planwith_fo_membership.domain.model.Entitlement;
import com.planwith.planwith_fo_membership.domain.model.Membership;
import com.planwith.planwith_fo_membership.domain.model.MembershipPayment;
import com.planwith.planwith_fo_membership.domain.model.MembershipSubscription;
import com.planwith.planwith_fo_membership.domain.model.PaymentStatus;
import com.planwith.planwith_fo_membership.domain.model.SubscriptionStatus;
import com.planwith.planwith_fo_membership.domain.model.vo.AdminUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.PaymentUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;

class CancelSubscriptionServiceTest {

	private final MemberUuid memberUuid = MemberUuid.from("11111111-1111-1111-1111-111111111111");
	private final CreatorUuid creatorUuid = CreatorUuid.from("22222222-2222-2222-2222-222222222222");
	private final MembershipUuid membershipUuid = MembershipUuid.from("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
	private final SubscriptionUuid subscriptionUuid = SubscriptionUuid.from("55555555-5555-5555-5555-555555555555");
	private final Instant startedAt = Instant.parse("2026-08-22T00:01:00Z");
	private final Instant canceledAt = Instant.parse("2026-08-22T00:10:00Z");

	private InMemoryLoadMembershipPort membershipPort;
	private InMemoryLoadSubscriptionPort subscriptionPort;
	private InMemoryEntitlementCacheAdapter entitlementCacheAdapter;
	private InMemoryMembershipEventOutboxPort outboxPort;
	private InMemoryPaymentPort paymentPort;
	private CancelSubscriptionService cancelSubscriptionService;
	private CheckContentAccessQueryService checkContentAccessQueryService;

	@BeforeEach
	void setUp() {
		membershipPort = new InMemoryLoadMembershipPort();
		subscriptionPort = new InMemoryLoadSubscriptionPort();
		entitlementCacheAdapter = new InMemoryEntitlementCacheAdapter();
		outboxPort = new InMemoryMembershipEventOutboxPort();
		paymentPort = new InMemoryPaymentPort();
		cancelSubscriptionService = new CancelSubscriptionService(
				subscriptionPort,
				subscriptionPort,
				membershipPort,
				new RevokeEntitlementService(entitlementCacheAdapter),
				outboxPort,
				new ObjectMapper().findAndRegisterModules()
		);
		checkContentAccessQueryService = new CheckContentAccessQueryService(entitlementCacheAdapter, subscriptionPort);
		Membership membership = Membership.apply(
				membershipUuid,
				creatorUuid,
				"윤휘명의 여행 멤버십",
				"월간 멤버십",
				100,
				Instant.parse("2026-08-22T00:00:00Z")
		).approve(AdminUuid.from("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"));
		membershipPort.save(membership);
		subscriptionPort.registerMembership(membership);
	}

	@Test
	void cancelsActiveSubscriptionWithoutRefund() {
		saveActiveSubscription();
		entitlementCacheAdapter.save(Entitlement.cachedActive(memberUuid, creatorUuid));
		paymentPort.save(MembershipPayment.ready(
				PaymentUuid.from("cccccccc-cccc-cccc-cccc-cccccccccccc"),
				subscriptionUuid,
				100L
		).succeed(startedAt));

		CancelSubscriptionResult result = cancelSubscriptionService.cancel(
				new CancelSubscriptionCommand(memberUuid, subscriptionUuid, canceledAt)
		);

		MembershipSubscription canceled = subscriptionPort.findByUuid(subscriptionUuid).orElseThrow();
		assertThat(result.status()).isEqualTo(SubscriptionStatus.INACTIVE);
		assertThat(result.endedAt()).isEqualTo(canceledAt);
		assertThat(canceled.status()).isEqualTo(SubscriptionStatus.INACTIVE);
		assertThat(canceled.endedAt()).isEqualTo(canceledAt);
		assertThat(entitlementCacheAdapter.find(memberUuid, creatorUuid)).isEmpty();
		assertThat(outboxPort.messages()).hasSize(1);
		assertThat(outboxPort.messages().get(0).eventType()).isEqualTo(MembershipEventTypes.MEMBERSHIP_CANCELED);
		assertThat(outboxPort.messages().get(0).payload()).contains("\"processedBy\":\"" + memberUuid + "\"");
		assertThat(paymentPort.findLatestBySubscription(subscriptionUuid).orElseThrow().paymentStatus())
				.isEqualTo(PaymentStatus.SUCCESS);
		ContentAccessResult access = checkContentAccessQueryService.check(
				new CheckContentAccessQuery(memberUuid, creatorUuid)
		);
		assertThat(access.allowed()).isFalse();
	}

	@Test
	void rejectsCancelWhenAlreadyInactive() {
		subscriptionPort.save(MembershipSubscription.active(subscriptionUuid, membershipUuid, memberUuid, startedAt)
				.deactivate(canceledAt));

		assertThatThrownBy(() -> cancelSubscriptionService.cancel(
				new CancelSubscriptionCommand(memberUuid, subscriptionUuid, Instant.parse("2026-08-22T00:20:00Z"))
		)).isInstanceOf(InvalidSubscriptionStateException.class)
				.hasMessage("이미 해지되었거나 만료된 구독입니다.");
		assertThat(outboxPort.messages()).isEmpty();
	}

	@Test
	void rejectsCancelWhenMemberDoesNotOwnSubscription() {
		saveActiveSubscription();

		assertThatThrownBy(() -> cancelSubscriptionService.cancel(new CancelSubscriptionCommand(
				MemberUuid.from("33333333-3333-3333-3333-333333333333"),
				subscriptionUuid,
				canceledAt
		))).isInstanceOf(ForbiddenCreatorException.class)
				.hasMessage("본인 구독만 해지할 수 있습니다.");
		assertThat(subscriptionPort.findByUuid(subscriptionUuid).orElseThrow().status())
				.isEqualTo(SubscriptionStatus.ACTIVE);
		assertThat(outboxPort.messages()).isEmpty();
	}

	private void saveActiveSubscription() {
		subscriptionPort.save(MembershipSubscription.active(subscriptionUuid, membershipUuid, memberUuid, startedAt));
	}
}
