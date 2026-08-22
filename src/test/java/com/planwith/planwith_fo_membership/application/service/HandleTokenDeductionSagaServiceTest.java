package com.planwith.planwith_fo_membership.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_membership.adapter.out.follow.InMemoryFollowQueryAdapter;
import com.planwith.planwith_fo_membership.adapter.out.persistence.membership.InMemoryLoadMembershipPort;
import com.planwith.planwith_fo_membership.adapter.out.persistence.outbox.InMemoryMembershipEventOutboxPort;
import com.planwith.planwith_fo_membership.adapter.out.persistence.payment.InMemoryPaymentPort;
import com.planwith.planwith_fo_membership.adapter.out.persistence.processed.InMemoryProcessedMembershipEventPort;
import com.planwith.planwith_fo_membership.adapter.out.persistence.revenue.InMemoryRevenueLedgerPort;
import com.planwith.planwith_fo_membership.adapter.out.persistence.revenue.InMemoryRevenuePort;
import com.planwith.planwith_fo_membership.adapter.out.persistence.saga.InMemoryMembershipSagaPort;
import com.planwith.planwith_fo_membership.adapter.out.persistence.subscription.InMemoryLoadSubscriptionPort;
import com.planwith.planwith_fo_membership.adapter.out.redis.InMemoryEntitlementCacheAdapter;
import com.planwith.planwith_fo_membership.application.command.HandleTokenDeductionFailedCommand;
import com.planwith.planwith_fo_membership.application.command.HandleTokenDeductionSucceededCommand;
import com.planwith.planwith_fo_membership.application.command.StartTokenPaymentCommand;
import com.planwith.planwith_fo_membership.application.query.StartTokenPaymentResult;
import com.planwith.planwith_fo_membership.domain.event.MembershipEventTypes;
import com.planwith.planwith_fo_membership.domain.model.Membership;
import com.planwith.planwith_fo_membership.domain.model.MembershipRevenue;
import com.planwith.planwith_fo_membership.domain.model.MembershipRevenueLedger;
import com.planwith.planwith_fo_membership.domain.model.MembershipSagaStatus;
import com.planwith.planwith_fo_membership.domain.model.PaymentStatus;
import com.planwith.planwith_fo_membership.domain.model.SubscriptionStatus;
import com.planwith.planwith_fo_membership.domain.model.vo.AdminUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.RevenueUuid;

class HandleTokenDeductionSagaServiceTest {

	private final MemberUuid memberUuid = MemberUuid.from("11111111-1111-1111-1111-111111111111");
	private final CreatorUuid creatorUuid = CreatorUuid.from("22222222-2222-2222-2222-222222222222");
	private final MembershipUuid membershipUuid = MembershipUuid.from("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

	private InMemoryFollowQueryAdapter followQueryPort;
	private InMemoryPaymentPort paymentPort;
	private InMemoryMembershipSagaPort sagaPort;
	private InMemoryLoadSubscriptionPort subscriptionPort;
	private InMemoryRevenuePort revenuePort;
	private InMemoryRevenueLedgerPort revenueLedgerPort;
	private InMemoryEntitlementCacheAdapter entitlementCacheAdapter;
	private InMemoryMembershipEventOutboxPort outboxPort;
	private StartTokenPaymentService startTokenPaymentService;
	private HandleTokenDeductionSucceededService succeededService;
	private HandleTokenDeductionFailedService failedService;

	@BeforeEach
	void setUp() {
		InMemoryLoadMembershipPort membershipPort = new InMemoryLoadMembershipPort();
		followQueryPort = new InMemoryFollowQueryAdapter();
		subscriptionPort = new InMemoryLoadSubscriptionPort();
		paymentPort = new InMemoryPaymentPort();
		sagaPort = new InMemoryMembershipSagaPort();
		revenuePort = new InMemoryRevenuePort();
		revenueLedgerPort = new InMemoryRevenueLedgerPort();
		entitlementCacheAdapter = new InMemoryEntitlementCacheAdapter();
		outboxPort = new InMemoryMembershipEventOutboxPort();
		InMemoryProcessedMembershipEventPort processedEventPort = new InMemoryProcessedMembershipEventPort();
		ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
		startTokenPaymentService = new StartTokenPaymentService(
				new ValidateJoinEligibilityService(membershipPort, followQueryPort, subscriptionPort),
				sagaPort,
				paymentPort,
				paymentPort,
				sagaPort,
				outboxPort,
				objectMapper
		);
		succeededService = new HandleTokenDeductionSucceededService(
				processedEventPort,
				paymentPort,
				paymentPort,
				sagaPort,
				sagaPort,
				membershipPort,
				subscriptionPort,
				subscriptionPort,
				revenuePort,
				revenuePort,
				revenueLedgerPort,
				revenueLedgerPort,
				new GrantEntitlementService(subscriptionPort, entitlementCacheAdapter),
				outboxPort,
				objectMapper
		);
		failedService = new HandleTokenDeductionFailedService(
				processedEventPort,
				paymentPort,
				paymentPort,
				sagaPort,
				sagaPort,
				subscriptionPort
		);
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
		revenuePort.save(MembershipRevenue.empty(
				RevenueUuid.from("66666666-6666-6666-6666-666666666666"),
				creatorUuid
		));
		followQueryPort.follow(memberUuid, creatorUuid);
	}

	@Test
	void tokenDeductionSuccessActivatesSubscriptionAndIncreasesRevenue() {
		StartTokenPaymentResult started = startTokenPaymentService.start(new StartTokenPaymentCommand(memberUuid, creatorUuid));
		assertThat(subscriptionPort.findByUuid(started.subscriptionUuid())).isEmpty();

		succeededService.handle(new HandleTokenDeductionSucceededCommand(
				UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"),
				memberUuid,
				creatorUuid,
				started.paymentUuid(),
				started.subscriptionUuid(),
				Instant.parse("2026-08-22T00:10:00Z")
		));

		assertThat(paymentPort.findByUuid(started.paymentUuid()).orElseThrow().paymentStatus())
				.isEqualTo(PaymentStatus.SUCCESS);
		assertThat(subscriptionPort.findByUuid(started.subscriptionUuid()).orElseThrow().status())
				.isEqualTo(SubscriptionStatus.ACTIVE);
		assertThat(sagaPort.findByPaymentUuid(started.paymentUuid()).orElseThrow().status())
				.isEqualTo(MembershipSagaStatus.ACTIVE);
		assertThat(revenuePort.findByCreator(creatorUuid).orElseThrow().totalRevenue()).isEqualTo(3_000L);
		assertThat(revenuePort.findByCreator(creatorUuid).orElseThrow().availableRevenue()).isEqualTo(3_000L);
		MembershipRevenueLedger ledger = revenueLedgerPort.findByPaymentUuid(started.paymentUuid()).orElseThrow();
		assertThat(ledger.grossKrw()).isEqualTo(10_000L);
		assertThat(ledger.companyShareKrw()).isEqualTo(7_000L);
		assertThat(ledger.creatorShareKrw()).isEqualTo(3_000L);
		assertThat(ledger.companyShareKrw() + ledger.creatorShareKrw()).isEqualTo(ledger.grossKrw());
		assertThat(entitlementCacheAdapter.find(memberUuid, creatorUuid).orElseThrow().allowed()).isTrue();
		assertThat(outboxPort.messages().stream().map(message -> message.eventType()))
				.contains(MembershipEventTypes.MEMBERSHIP_SUBSCRIBED);
	}

	@Test
	void tokenDeductionFailureMarksPaymentFailedAndDoesNotCreateSubscription() {
		StartTokenPaymentResult started = startTokenPaymentService.start(new StartTokenPaymentCommand(memberUuid, creatorUuid));

		failedService.handle(new HandleTokenDeductionFailedCommand(
				UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"),
				memberUuid,
				started.paymentUuid(),
				started.subscriptionUuid(),
				Instant.parse("2026-08-22T00:10:00Z")
		));

		assertThat(paymentPort.findByUuid(started.paymentUuid()).orElseThrow().paymentStatus())
				.isEqualTo(PaymentStatus.FAILED);
		assertThat(subscriptionPort.findByUuid(started.subscriptionUuid())).isEmpty();
		assertThat(sagaPort.findByPaymentUuid(started.paymentUuid()).orElseThrow().status())
				.isEqualTo(MembershipSagaStatus.FAILED);
		assertThat(revenuePort.findByCreator(creatorUuid).orElseThrow().totalRevenue()).isZero();
		assertThat(revenueLedgerPort.findByPaymentUuid(started.paymentUuid())).isEmpty();
		assertThat(entitlementCacheAdapter.find(memberUuid, creatorUuid)).isEmpty();
		assertThat(outboxPort.messages().stream().map(message -> message.eventType()))
				.containsExactly(MembershipEventTypes.TOKEN_DEDUCTION_REQUESTED);
	}

	@Test
	void duplicateSuccessEventDoesNotIncreaseRevenueTwice() {
		StartTokenPaymentResult started = startTokenPaymentService.start(new StartTokenPaymentCommand(memberUuid, creatorUuid));
		HandleTokenDeductionSucceededCommand command = new HandleTokenDeductionSucceededCommand(
				UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"),
				memberUuid,
				creatorUuid,
				started.paymentUuid(),
				started.subscriptionUuid(),
				Instant.parse("2026-08-22T00:10:00Z")
		);

		succeededService.handle(command);
		succeededService.handle(command);

		assertThat(revenuePort.findByCreator(creatorUuid).orElseThrow().totalRevenue()).isEqualTo(3_000L);
	}
}
