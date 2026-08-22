package com.planwith.planwith_fo_membership.application.event;

import java.time.Instant;

public record TokenDeductionRequestedEvent(
		String eventUuid,
		String memberUuid,
		String creatorUuid,
		String paymentUuid,
		String subscriptionUuid,
		long amount,
		String priceUnit,
		String referenceType,
		Instant requestedAt
) {
}
