package com.planwith.planwith_fo_membership.adapter.in.kafka.dto;

import java.time.Instant;

public record PaymentCompletedInboundEvent(
		String eventUuid,
		String memberUuid,
		String creatorUuid,
		String paymentUuid,
		String subscriptionUuid,
		Instant completedAt
) {
}
