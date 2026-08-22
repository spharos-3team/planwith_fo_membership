package com.planwith.planwith_fo_membership.application.command;

import java.util.Objects;

import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;

public record GrantEntitlementCommand(
		MemberUuid memberUuid,
		CreatorUuid creatorUuid,
		SubscriptionUuid subscriptionUuid
) {

	public GrantEntitlementCommand {
		Objects.requireNonNull(memberUuid, "Member UUID is required.");
		Objects.requireNonNull(creatorUuid, "Creator UUID is required.");
		Objects.requireNonNull(subscriptionUuid, "Subscription UUID is required.");
	}
}
