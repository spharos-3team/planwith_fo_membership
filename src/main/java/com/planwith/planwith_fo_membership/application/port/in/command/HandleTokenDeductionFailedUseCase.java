package com.planwith.planwith_fo_membership.application.port.in.command;

import com.planwith.planwith_fo_membership.application.command.HandleTokenDeductionFailedCommand;

public interface HandleTokenDeductionFailedUseCase {

	void handle(HandleTokenDeductionFailedCommand command);
}
