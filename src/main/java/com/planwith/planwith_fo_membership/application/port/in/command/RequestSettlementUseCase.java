package com.planwith.planwith_fo_membership.application.port.in.command;

import com.planwith.planwith_fo_membership.application.command.RequestSettlementCommand;
import com.planwith.planwith_fo_membership.application.query.RequestSettlementResult;

public interface RequestSettlementUseCase {

	RequestSettlementResult request(RequestSettlementCommand command);
}
