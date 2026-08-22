package com.planwith.planwith_fo_membership.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_membership.adapter.out.persistence.processed.InMemoryProcessedMembershipEventPort;
import com.planwith.planwith_fo_membership.application.command.HandlePaymentCompletedCommand;
import com.planwith.planwith_fo_membership.application.command.HandlePaymentFailedCommand;
import com.planwith.planwith_fo_membership.application.command.HandlePaymentRefundedCommand;
import com.planwith.planwith_fo_membership.domain.event.MembershipEventTypes;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.PaymentUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;

class HandleInboundPaymentEventServiceTest {

	private final MemberUuid memberUuid = MemberUuid.from("11111111-1111-1111-1111-111111111111");
	private final PaymentUuid paymentUuid = PaymentUuid.from("cccccccc-cccc-cccc-cccc-cccccccccccc");
	private final SubscriptionUuid subscriptionUuid = SubscriptionUuid.from("55555555-5555-5555-5555-555555555555");

	private InMemoryProcessedMembershipEventPort processedEventPort;
	private HandlePaymentCompletedService completedService;
	private HandlePaymentFailedService failedService;
	private HandlePaymentRefundedService refundedService;

	@BeforeEach
	void setUp() {
		processedEventPort = new InMemoryProcessedMembershipEventPort();
		completedService = new HandlePaymentCompletedService(processedEventPort);
		failedService = new HandlePaymentFailedService(processedEventPort);
		refundedService = new HandlePaymentRefundedService(processedEventPort);
	}

	@Test
	void paymentCompletedIsIdempotentByEventUuidAndPaymentUuid() {
		completedService.handle(completed(
				UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
				paymentUuid
		));
		completedService.handle(completed(
				UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
				paymentUuid
		));
		completedService.handle(completed(
				UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
				paymentUuid
		));

		assertThat(processedEventPort.existsByEventUuid(
				UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
		)).isTrue();
		assertThat(processedEventPort.existsByPaymentUuidAndEventType(
				paymentUuid.value(),
				MembershipEventTypes.PAYMENT_COMPLETED
		)).isTrue();
		assertThat(processedEventPort.existsByEventUuid(
				UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")
		)).isFalse();
	}

	@Test
	void paymentFailedAndRefundedAreIdempotentByPaymentUuid() {
		failedService.handle(failed(UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc")));
		failedService.handle(failed(UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd")));
		refundedService.handle(refunded(UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee")));
		refundedService.handle(refunded(UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff")));

		assertThat(processedEventPort.existsByPaymentUuidAndEventType(
				paymentUuid.value(),
				MembershipEventTypes.PAYMENT_FAILED
		)).isTrue();
		assertThat(processedEventPort.existsByPaymentUuidAndEventType(
				paymentUuid.value(),
				MembershipEventTypes.PAYMENT_REFUNDED
		)).isTrue();
		assertThat(processedEventPort.existsByEventUuid(
				UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd")
		)).isFalse();
		assertThat(processedEventPort.existsByEventUuid(
				UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff")
		)).isFalse();
	}

	private HandlePaymentCompletedCommand completed(UUID eventUuid, PaymentUuid payment) {
		return new HandlePaymentCompletedCommand(
				eventUuid,
				memberUuid,
				CreatorUuid.from("22222222-2222-2222-2222-222222222222"),
				payment,
				subscriptionUuid,
				Instant.parse("2026-08-22T00:10:00Z")
		);
	}

	private HandlePaymentFailedCommand failed(UUID eventUuid) {
		return new HandlePaymentFailedCommand(
				eventUuid,
				memberUuid,
				paymentUuid,
				subscriptionUuid,
				Instant.parse("2026-08-22T00:10:00Z")
		);
	}

	private HandlePaymentRefundedCommand refunded(UUID eventUuid) {
		return new HandlePaymentRefundedCommand(
				eventUuid,
				memberUuid,
				paymentUuid,
				subscriptionUuid,
				Instant.parse("2026-08-22T00:10:00Z")
		);
	}
}
