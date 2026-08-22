package com.planwith.planwith_fo_membership.application.command;

import java.time.Instant;
import java.util.Objects;

import com.planwith.planwith_fo_membership.domain.model.vo.AdminUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SettlementUuid;

public record ApproveSettlementCommand(
		SettlementUuid settlementUuid,
		AdminUuid adminUuid,
		Instant processedAt
) {

	public ApproveSettlementCommand {
		Objects.requireNonNull(settlementUuid, "Settlement UUID is required.");
		Objects.requireNonNull(adminUuid, "Admin UUID is required.");
	}
}
