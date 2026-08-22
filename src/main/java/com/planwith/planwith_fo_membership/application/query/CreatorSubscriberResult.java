package com.planwith.planwith_fo_membership.application.query;

import java.time.Instant;

import com.planwith.planwith_fo_membership.domain.model.SubscriptionStatus;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;

public record CreatorSubscriberResult(
		SubscriptionUuid subscriptionUuid,
		MemberUuid memberUuid,
		SubscriptionStatus status,
		Instant startedAt,
		Instant endedAt
) {
}
