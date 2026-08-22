package com.planwith.planwith_fo_membership.application.command;

import java.time.Instant;
import java.util.Objects;

import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;

public record CancelSubscriptionCommand(
		MemberUuid memberUuid,
		SubscriptionUuid subscriptionUuid,
		Instant canceledAt
) {

	public CancelSubscriptionCommand {
		Objects.requireNonNull(memberUuid, "Member UUID is required.");
		Objects.requireNonNull(subscriptionUuid, "Subscription UUID is required.");
	}
}
