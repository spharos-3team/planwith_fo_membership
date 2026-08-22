package com.planwith.planwith_fo_membership.application.port.in.command;

import com.planwith.planwith_fo_membership.application.command.CancelSubscriptionCommand;

public interface CancelSubscriptionUseCase {

	void cancel(CancelSubscriptionCommand command);
}
