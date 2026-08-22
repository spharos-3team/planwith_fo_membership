package com.planwith.planwith_fo_membership.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.PaymentUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;

public final class MembershipSaga {

	private final UUID sagaUuid;
	private final MemberUuid memberUuid;
	private final CreatorUuid creatorUuid;
	private final SubscriptionUuid subscriptionUuid;
	private final PaymentUuid paymentUuid;
	private final MembershipSagaStatus status;
	private final Instant updatedAt;

	private MembershipSaga(
			UUID sagaUuid,
			MemberUuid memberUuid,
			CreatorUuid creatorUuid,
			SubscriptionUuid subscriptionUuid,
			PaymentUuid paymentUuid,
			MembershipSagaStatus status,
			Instant updatedAt
	) {
		this.sagaUuid = Objects.requireNonNull(sagaUuid, "Saga UUID is required.");
		this.memberUuid = Objects.requireNonNull(memberUuid, "Member UUID is required.");
		this.creatorUuid = Objects.requireNonNull(creatorUuid, "Creator UUID is required.");
		this.subscriptionUuid = Objects.requireNonNull(subscriptionUuid, "Subscription UUID is required.");
		this.paymentUuid = paymentUuid;
		this.status = Objects.requireNonNull(status, "Saga status is required.");
		this.updatedAt = Objects.requireNonNull(updatedAt, "Updated at is required.");
	}

	public static MembershipSaga subscribeRequested(
			UUID sagaUuid,
			MemberUuid memberUuid,
			CreatorUuid creatorUuid,
			SubscriptionUuid subscriptionUuid,
			Instant updatedAt
	) {
		return new MembershipSaga(
				sagaUuid,
				memberUuid,
				creatorUuid,
				subscriptionUuid,
				null,
				MembershipSagaStatus.SUBSCRIBE_REQUESTED,
				updatedAt
		);
	}

	public static MembershipSaga restore(
			UUID sagaUuid,
			MemberUuid memberUuid,
			CreatorUuid creatorUuid,
			SubscriptionUuid subscriptionUuid,
			PaymentUuid paymentUuid,
			MembershipSagaStatus status,
			Instant updatedAt
	) {
		return new MembershipSaga(
				sagaUuid,
				memberUuid,
				creatorUuid,
				subscriptionUuid,
				paymentUuid,
				status,
				updatedAt
		);
	}

	public MembershipSaga paymentPending(PaymentUuid requestedPaymentUuid, Instant updatedAt) {
		return new MembershipSaga(
				sagaUuid,
				memberUuid,
				creatorUuid,
				subscriptionUuid,
				requestedPaymentUuid,
				MembershipSagaStatus.PAYMENT_PENDING,
				updatedAt
		);
	}

	public MembershipSaga paymentCompleted(Instant updatedAt) {
		return new MembershipSaga(
				sagaUuid,
				memberUuid,
				creatorUuid,
				subscriptionUuid,
				paymentUuid,
				MembershipSagaStatus.PAYMENT_COMPLETED,
				updatedAt
		);
	}

	public MembershipSaga activated(Instant updatedAt) {
		return new MembershipSaga(
				sagaUuid,
				memberUuid,
				creatorUuid,
				subscriptionUuid,
				paymentUuid,
				MembershipSagaStatus.ACTIVE,
				updatedAt
		);
	}

	public MembershipSaga compensating(Instant updatedAt) {
		return new MembershipSaga(
				sagaUuid,
				memberUuid,
				creatorUuid,
				subscriptionUuid,
				paymentUuid,
				MembershipSagaStatus.COMPENSATING,
				updatedAt
		);
	}

	public MembershipSaga failed(Instant updatedAt) {
		return new MembershipSaga(
				sagaUuid,
				memberUuid,
				creatorUuid,
				subscriptionUuid,
				paymentUuid,
				MembershipSagaStatus.FAILED,
				updatedAt
		);
	}

	public boolean isPaymentInProgress() {
		return status == MembershipSagaStatus.SUBSCRIBE_REQUESTED
				|| status == MembershipSagaStatus.PAYMENT_PENDING;
	}

	public UUID sagaUuid() {
		return sagaUuid;
	}

	public MemberUuid memberUuid() {
		return memberUuid;
	}

	public CreatorUuid creatorUuid() {
		return creatorUuid;
	}

	public SubscriptionUuid subscriptionUuid() {
		return subscriptionUuid;
	}

	public PaymentUuid paymentUuid() {
		return paymentUuid;
	}

	public MembershipSagaStatus status() {
		return status;
	}

	public Instant updatedAt() {
		return updatedAt;
	}
}
