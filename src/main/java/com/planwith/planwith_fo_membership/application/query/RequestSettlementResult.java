package com.planwith.planwith_fo_membership.application.query;

import java.time.Instant;

import com.planwith.planwith_fo_membership.domain.model.SettlementStatus;
import com.planwith.planwith_fo_membership.domain.model.vo.SettlementUuid;

public record RequestSettlementResult(
		SettlementUuid settlementUuid,
		SettlementStatus status,
		long settlementAmount,
		long availableRevenue,
		long reservedRevenue,
		Instant requestedAt
) {
}
