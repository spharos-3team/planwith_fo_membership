package com.planwith.planwith_fo_membership.application.command;

import java.time.Instant;
import java.util.Objects;

import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;

public record ExpireSubscriptionCommand(
		SubscriptionUuid subscriptionUuid,
		Instant expiredAt
) {

	public ExpireSubscriptionCommand {
		Objects.requireNonNull(subscriptionUuid, "Subscription UUID is required.");
	}
}
