package com.planwith.planwith_fo_membership.adapter.in.kafka.dto;

import java.time.Instant;

public record TokenDeductionFailedInboundEvent(
		String eventUuid,
		String memberUuid,
		String paymentUuid,
		String subscriptionUuid,
		Instant failedAt
) {
}
