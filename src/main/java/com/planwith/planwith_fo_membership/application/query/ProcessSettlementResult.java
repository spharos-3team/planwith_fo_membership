package com.planwith.planwith_fo_membership.application.query;

import java.time.Instant;

import com.planwith.planwith_fo_membership.domain.model.SettlementStatus;
import com.planwith.planwith_fo_membership.domain.model.vo.AdminUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SettlementUuid;

public record ProcessSettlementResult(
		SettlementUuid settlementUuid,
		CreatorUuid creatorUuid,
		AdminUuid adminUuid,
		SettlementStatus status,
		long settlementAmount,
		long availableRevenue,
		long reservedRevenue,
		long settledRevenue,
		Instant approvedAt,
		Instant paidAt,
		String rejectReason
) {
}
