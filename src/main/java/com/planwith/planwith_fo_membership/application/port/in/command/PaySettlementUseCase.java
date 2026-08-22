package com.planwith.planwith_fo_membership.application.port.in.command;

import com.planwith.planwith_fo_membership.application.command.PaySettlementCommand;
import com.planwith.planwith_fo_membership.application.query.ProcessSettlementResult;

public interface PaySettlementUseCase {

	ProcessSettlementResult pay(PaySettlementCommand command);
}
