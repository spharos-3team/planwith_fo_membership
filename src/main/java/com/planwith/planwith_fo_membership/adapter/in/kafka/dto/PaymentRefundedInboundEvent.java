package com.planwith.planwith_fo_membership.adapter.in.kafka.dto;

import java.time.Instant;

public record PaymentRefundedInboundEvent(
		String eventUuid,
		String memberUuid,
		String paymentUuid,
		String subscriptionUuid,
		Instant refundedAt
) {
}
