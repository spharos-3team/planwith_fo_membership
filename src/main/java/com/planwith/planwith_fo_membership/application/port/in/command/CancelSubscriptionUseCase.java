package com.planwith.planwith_fo_membership.application.port.in.command;

import com.planwith.planwith_fo_membership.application.command.CancelSubscriptionCommand;
import com.planwith.planwith_fo_membership.application.query.CancelSubscriptionResult;

public interface CancelSubscriptionUseCase {

	CancelSubscriptionResult cancel(CancelSubscriptionCommand command);
}
