package com.planwith.planwith_fo_membership.application.port.in.command;

import com.planwith.planwith_fo_membership.application.command.HandlePaymentCompletedCommand;

public interface HandlePaymentCompletedUseCase {

	void handle(HandlePaymentCompletedCommand command);
}
