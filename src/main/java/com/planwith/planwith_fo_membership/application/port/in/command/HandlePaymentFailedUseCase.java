package com.planwith.planwith_fo_membership.application.port.in.command;

import com.planwith.planwith_fo_membership.application.command.HandlePaymentFailedCommand;

public interface HandlePaymentFailedUseCase {

	void handle(HandlePaymentFailedCommand command);
}
