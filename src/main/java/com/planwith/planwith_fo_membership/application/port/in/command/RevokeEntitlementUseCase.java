package com.planwith.planwith_fo_membership.application.port.in.command;

import com.planwith.planwith_fo_membership.application.command.RevokeEntitlementCommand;

public interface RevokeEntitlementUseCase {

	void revoke(RevokeEntitlementCommand command);
}
