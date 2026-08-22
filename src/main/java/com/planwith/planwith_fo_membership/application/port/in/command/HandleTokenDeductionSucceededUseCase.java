package com.planwith.planwith_fo_membership.application.port.in.command;

import com.planwith.planwith_fo_membership.application.command.HandleTokenDeductionSucceededCommand;

public interface HandleTokenDeductionSucceededUseCase {

	void handle(HandleTokenDeductionSucceededCommand command);
}
