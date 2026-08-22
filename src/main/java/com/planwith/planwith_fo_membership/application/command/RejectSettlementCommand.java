package com.planwith.planwith_fo_membership.application.command;

import java.util.Objects;

import com.planwith.planwith_fo_membership.domain.model.vo.AdminUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SettlementUuid;

public record RejectSettlementCommand(
		SettlementUuid settlementUuid,
		AdminUuid adminUuid,
		String rejectReason
) {

	public RejectSettlementCommand {
		Objects.requireNonNull(settlementUuid, "Settlement UUID is required.");
		Objects.requireNonNull(adminUuid, "Admin UUID is required.");
	}
}
