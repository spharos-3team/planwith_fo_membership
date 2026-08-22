package com.planwith.planwith_fo_membership.application.event;

import java.time.Instant;

public record SettlementCompletedEvent(
		String eventUuid,
		String creatorUuid,
		String revenueUuid,
		String settlementUuid,
		long settlementAmount,
		String processedBy,
		Instant paidAt
) {
}
