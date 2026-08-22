package com.planwith.planwith_fo_membership.domain.model;

import java.util.Objects;

import com.planwith.planwith_fo_membership.domain.exception.InvalidSubscriptionStateException;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;
import com.planwith.planwith_fo_membership.domain.service.EntitlementPolicy;

public final class Entitlement {

	private final MemberUuid memberUuid;
	private final CreatorUuid creatorUuid;
	private final SubscriptionUuid subscriptionUuid;
	private final boolean allowed;

	private Entitlement(
			MemberUuid memberUuid,
			CreatorUuid creatorUuid,
			SubscriptionUuid subscriptionUuid,
			boolean allowed
	) {
		this.memberUuid = Objects.requireNonNull(memberUuid, "Member UUID is required.");
		this.creatorUuid = Objects.requireNonNull(creatorUuid, "Creator UUID is required.");
		this.subscriptionUuid = subscriptionUuid;
		this.allowed = allowed;
	}

	public static Entitlement grant(MembershipSubscription subscription, CreatorUuid creatorUuid) {
		if (!EntitlementPolicy.canGrant(subscription)) {
			throw new InvalidSubscriptionStateException("활성 구독에만 접근 권한을 부여할 수 있습니다.");
		}
		return new Entitlement(
				subscription.memberUuid(),
				creatorUuid,
				subscription.subscriptionUuid(),
				true
		);
	}

	public static Entitlement from(MembershipSubscription subscription, CreatorUuid creatorUuid) {
		Objects.requireNonNull(subscription, "Subscription is required.");
		return new Entitlement(
				subscription.memberUuid(),
				creatorUuid,
				subscription.subscriptionUuid(),
				EntitlementPolicy.canAccess(subscription)
		);
	}

	public static Entitlement cachedActive(MemberUuid memberUuid, CreatorUuid creatorUuid) {
		return new Entitlement(memberUuid, creatorUuid, null, true);
	}

	public static Entitlement denied(MemberUuid memberUuid, CreatorUuid creatorUuid) {
		return new Entitlement(memberUuid, creatorUuid, null, false);
	}

	public String status() {
		return allowed ? EntitlementPolicy.CACHE_VALUE_ACTIVE : null;
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

	public boolean allowed() {
		return allowed;
	}
}
