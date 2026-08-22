package com.planwith.planwith_fo_membership.application.port.in.command;

import com.planwith.planwith_fo_membership.application.command.SubscribeMembershipCommand;

public interface SubscribeMembershipUseCase {

	void subscribe(SubscribeMembershipCommand command);
}
