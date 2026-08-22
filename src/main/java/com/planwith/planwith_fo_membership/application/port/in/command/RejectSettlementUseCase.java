package com.planwith.planwith_fo_membership.application.port.in.command;

import com.planwith.planwith_fo_membership.application.command.RejectSettlementCommand;
import com.planwith.planwith_fo_membership.application.query.ProcessSettlementResult;

public interface RejectSettlementUseCase {

	ProcessSettlementResult reject(RejectSettlementCommand command);
}
