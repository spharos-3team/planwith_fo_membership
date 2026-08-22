package com.planwith.planwith_fo_membership.application.port.in.command;

import com.planwith.planwith_fo_membership.application.command.StartTokenPaymentCommand;
import com.planwith.planwith_fo_membership.application.query.StartTokenPaymentResult;

public interface StartTokenPaymentUseCase {

	StartTokenPaymentResult start(StartTokenPaymentCommand command);
}
