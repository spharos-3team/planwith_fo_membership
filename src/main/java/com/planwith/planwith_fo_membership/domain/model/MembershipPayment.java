package com.planwith.planwith_fo_membership.domain.model;

import java.time.Instant;
import java.util.Objects;

import com.planwith.planwith_fo_membership.domain.exception.InvalidPaymentStateException;
import com.planwith.planwith_fo_membership.domain.model.vo.PaymentUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;

public final class MembershipPayment {

	private final PaymentUuid paymentUuid;
	private final SubscriptionUuid subscriptionUuid;
	private final long amount;
	private final PaymentStatus paymentStatus;
	private final Instant paidAt;

	private MembershipPayment(
			PaymentUuid paymentUuid,
			SubscriptionUuid subscriptionUuid,
			long amount,
			PaymentStatus paymentStatus,
			Instant paidAt
	) {
		this.paymentUuid = Objects.requireNonNull(paymentUuid, "Payment UUID is required.");
		this.subscriptionUuid = Objects.requireNonNull(subscriptionUuid, "Subscription UUID is required.");
		this.amount = requireAmount(amount);
		this.paymentStatus = Objects.requireNonNull(paymentStatus, "Payment status is required.");
		this.paidAt = paidAt;
	}

	public static MembershipPayment ready(
			PaymentUuid paymentUuid,
			SubscriptionUuid subscriptionUuid,
			long amount
	) {
		return new MembershipPayment(paymentUuid, subscriptionUuid, amount, PaymentStatus.READY, null);
	}

	public static MembershipPayment restore(
			PaymentUuid paymentUuid,
			SubscriptionUuid subscriptionUuid,
			long amount,
			PaymentStatus paymentStatus,
			Instant paidAt
	) {
		return new MembershipPayment(paymentUuid, subscriptionUuid, amount, paymentStatus, paidAt);
	}

	public MembershipPayment succeed(Instant paidAt) {
		ensureReady();
		return new MembershipPayment(
				paymentUuid,
				subscriptionUuid,
				amount,
				PaymentStatus.SUCCESS,
				Objects.requireNonNull(paidAt, "Paid at is required.")
		);
	}

	public MembershipPayment fail() {
		ensureReady();
		return new MembershipPayment(paymentUuid, subscriptionUuid, amount, PaymentStatus.FAILED, null);
	}

	public MembershipPayment cancel() {
		ensureReady();
		return new MembershipPayment(paymentUuid, subscriptionUuid, amount, PaymentStatus.CANCELED, null);
	}

	public PaymentUuid paymentUuid() {
		return paymentUuid;
	}

	public SubscriptionUuid subscriptionUuid() {
		return subscriptionUuid;
	}

	public long amount() {
		return amount;
	}

	public PaymentStatus paymentStatus() {
		return paymentStatus;
	}

	public Instant paidAt() {
		return paidAt;
	}

	private void ensureReady() {
		if (paymentStatus != PaymentStatus.READY) {
			throw new InvalidPaymentStateException("대기 중인 결제만 상태를 변경할 수 있습니다.");
		}
	}

	private static long requireAmount(long amount) {
		if (amount <= 0) {
			throw new InvalidPaymentStateException("결제 금액은 0보다 커야 합니다.");
		}
		return amount;
	}
}
