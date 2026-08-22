package com.planwith.planwith_fo_membership.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_membership.domain.exception.InvalidPaymentStateException;
import com.planwith.planwith_fo_membership.domain.model.vo.PaymentUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;

class MembershipPaymentTest {

	private final PaymentUuid paymentUuid = new PaymentUuid(UUID.fromString("44444444-4444-4444-4444-444444444444"));
	private final SubscriptionUuid subscriptionUuid = new SubscriptionUuid(UUID.fromString("33333333-3333-3333-3333-333333333333"));

	@Test
	void readyThenSucceed() {
		MembershipPayment ready = MembershipPayment.ready(paymentUuid, subscriptionUuid, 12900L);
		MembershipPayment success = ready.succeed(Instant.parse("2026-08-22T00:02:00Z"));

		assertThat(ready.paymentStatus()).isEqualTo(PaymentStatus.READY);
		assertThat(success.paymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
		assertThat(success.paidAt()).isEqualTo(Instant.parse("2026-08-22T00:02:00Z"));
	}

	@Test
	void readyCanFailOrCancel() {
		assertThat(MembershipPayment.ready(paymentUuid, subscriptionUuid, 12900L).fail().paymentStatus())
				.isEqualTo(PaymentStatus.FAILED);
		assertThat(MembershipPayment.ready(paymentUuid, subscriptionUuid, 12900L).cancel().paymentStatus())
				.isEqualTo(PaymentStatus.CANCELED);
	}

	@Test
	void succeedRejectsNonReadyPayment() {
		MembershipPayment success = MembershipPayment.ready(paymentUuid, subscriptionUuid, 12900L)
				.succeed(Instant.parse("2026-08-22T00:02:00Z"));

		assertThatThrownBy(() -> success.fail())
				.isInstanceOf(InvalidPaymentStateException.class);
	}
}
