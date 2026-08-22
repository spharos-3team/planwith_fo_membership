package com.planwith.planwith_fo_membership.application.event;

import java.time.Instant;

public record MembershipSubscribedEvent(
		String eventUuid,
		String memberUuid,
		String creatorUuid,
		String membershipUuid,
		String subscriptionUuid,
		String paymentUuid,
		long amount,
		String priceUnit,
		Instant subscribedAt
) {
}
