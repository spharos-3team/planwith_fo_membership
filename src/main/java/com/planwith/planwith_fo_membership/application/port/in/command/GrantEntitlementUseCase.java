package com.planwith.planwith_fo_membership.application.port.in.command;

import com.planwith.planwith_fo_membership.application.command.GrantEntitlementCommand;

public interface GrantEntitlementUseCase {

	void grant(GrantEntitlementCommand command);
}
