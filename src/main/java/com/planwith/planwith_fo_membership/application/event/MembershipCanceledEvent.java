package com.planwith.planwith_fo_membership.application.event;

import java.time.Instant;

public record MembershipCanceledEvent(
		String eventUuid,
		String memberUuid,
		String creatorUuid,
		String membershipUuid,
		String subscriptionUuid,
		String processedBy,
		Instant canceledAt
) {
}
