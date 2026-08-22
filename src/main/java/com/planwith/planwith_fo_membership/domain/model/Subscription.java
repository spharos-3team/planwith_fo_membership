package com.planwith.planwith_fo_membership.domain.model;

import java.time.Instant;
import java.util.Objects;

import com.planwith.planwith_fo_membership.domain.exception.InvalidSubscriptionStateException;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;

public final class Subscription {

	private final SubscriptionUuid subscriptionUuid;
	private final MembershipUuid membershipUuid;
	private final MemberUuid memberUuid;
	private final SubscriptionStatus status;
	private final Instant startedAt;
	private final Instant endedAt;

	private Subscription(
			SubscriptionUuid subscriptionUuid,
			MembershipUuid membershipUuid,
			MemberUuid memberUuid,
			SubscriptionStatus status,
			Instant startedAt,
			Instant endedAt
	) {
		this.subscriptionUuid = Objects.requireNonNull(subscriptionUuid, "Subscription UUID is required.");
		this.membershipUuid = Objects.requireNonNull(membershipUuid, "Membership UUID is required.");
		this.memberUuid = Objects.requireNonNull(memberUuid, "Member UUID is required.");
		this.status = Objects.requireNonNull(status, "Subscription status is required.");
		this.startedAt = Objects.requireNonNull(startedAt, "Started at is required.");
		this.endedAt = endedAt;
	}

	public static Subscription active(
			SubscriptionUuid subscriptionUuid,
			MembershipUuid membershipUuid,
			MemberUuid memberUuid,
			Instant startedAt
	) {
		return new Subscription(
				subscriptionUuid,
				membershipUuid,
				memberUuid,
				SubscriptionStatus.ACTIVE,
				startedAt,
				null
		);
	}

	public static Subscription restore(
			SubscriptionUuid subscriptionUuid,
			MembershipUuid membershipUuid,
			MemberUuid memberUuid,
			SubscriptionStatus status,
			Instant startedAt,
			Instant endedAt
	) {
		return new Subscription(
				subscriptionUuid,
				membershipUuid,
				memberUuid,
				status,
				startedAt,
				endedAt
		);
	}

	public Subscription deactivate(Instant endedAt) {
		if (status != SubscriptionStatus.ACTIVE) {
			throw new InvalidSubscriptionStateException("활성 구독만 비활성화할 수 있습니다.");
		}
		return new Subscription(
				subscriptionUuid,
				membershipUuid,
				memberUuid,
				SubscriptionStatus.INACTIVE,
				startedAt,
				Objects.requireNonNull(endedAt, "Ended at is required.")
		);
	}

	public SubscriptionUuid subscriptionUuid() {
		return subscriptionUuid;
	}

	public MembershipUuid membershipUuid() {
		return membershipUuid;
	}

	public MemberUuid memberUuid() {
		return memberUuid;
	}

	public SubscriptionStatus status() {
		return status;
	}

	public Instant startedAt() {
		return startedAt;
	}

	public Instant endedAt() {
		return endedAt;
	}
}
