package com.planwith.planwith_fo_membership.application.command;

import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;

public record ExpireSubscriptionCommand(
		SubscriptionUuid subscriptionUuid
) {
}
