package com.planwith.planwith_fo_membership.application.port.in.command;

import com.planwith.planwith_fo_membership.application.command.HandlePaymentRefundedCommand;

public interface HandlePaymentRefundedUseCase {

	void handle(HandlePaymentRefundedCommand command);
}
