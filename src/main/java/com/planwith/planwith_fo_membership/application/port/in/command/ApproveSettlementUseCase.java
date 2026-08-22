package com.planwith.planwith_fo_membership.application.port.in.command;

import com.planwith.planwith_fo_membership.application.command.ApproveSettlementCommand;
import com.planwith.planwith_fo_membership.application.query.ProcessSettlementResult;

public interface ApproveSettlementUseCase {

	ProcessSettlementResult approve(ApproveSettlementCommand command);
}
