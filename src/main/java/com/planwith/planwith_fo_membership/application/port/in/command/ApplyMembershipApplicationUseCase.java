package com.planwith.planwith_fo_membership.application.port.in.command;

import com.planwith.planwith_fo_membership.application.command.ValidateMembershipApplicationCommand;
import com.planwith.planwith_fo_membership.application.query.ApplyMembershipApplicationResult;

public interface ApplyMembershipApplicationUseCase {

	ApplyMembershipApplicationResult apply(ValidateMembershipApplicationCommand command);
}
