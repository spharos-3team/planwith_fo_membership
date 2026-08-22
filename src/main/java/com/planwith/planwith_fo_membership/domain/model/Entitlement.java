package com.planwith.planwith_fo_membership.domain.model;

import java.util.Objects;

import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.service.EntitlementPolicy;

public final class Entitlement {

	private final MemberUuid memberUuid;
	private final CreatorUuid creatorUuid;
	private final boolean allowed;

	private Entitlement(MemberUuid memberUuid, CreatorUuid creatorUuid, boolean allowed) {
		this.memberUuid = Objects.requireNonNull(memberUuid, "Member UUID is required.");
		this.creatorUuid = Objects.requireNonNull(creatorUuid, "Creator UUID is required.");
		this.allowed = allowed;
	}

	public static Entitlement from(Subscription subscription, CreatorUuid creatorUuid) {
		Objects.requireNonNull(subscription, "Subscription is required.");
		return new Entitlement(
				subscription.memberUuid(),
				creatorUuid,
				EntitlementPolicy.canAccess(subscription)
		);
	}

	public static Entitlement denied(MemberUuid memberUuid, CreatorUuid creatorUuid) {
		return new Entitlement(memberUuid, creatorUuid, false);
	}

	public MemberUuid memberUuid() {
		return memberUuid;
	}

	public CreatorUuid creatorUuid() {
		return creatorUuid;
	}

	public boolean allowed() {
		return allowed;
	}
}
