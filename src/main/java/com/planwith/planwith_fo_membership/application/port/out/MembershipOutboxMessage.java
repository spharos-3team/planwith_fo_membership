package com.planwith.planwith_fo_membership.application.port.out;

import java.time.Instant;

public record MembershipOutboxMessage(
		String eventUuid,
		String aggregateType,
		String aggregateUuid,
		String eventType,
		String payload,
		Instant occurredAt
) {
}
