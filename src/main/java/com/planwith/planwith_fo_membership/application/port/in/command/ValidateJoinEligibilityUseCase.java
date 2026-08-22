package com.planwith.planwith_fo_membership.application.port.in.command;

import com.planwith.planwith_fo_membership.application.command.ValidateJoinEligibilityCommand;
import com.planwith.planwith_fo_membership.application.query.ValidateJoinEligibilityResult;

public interface ValidateJoinEligibilityUseCase {

	ValidateJoinEligibilityResult validate(ValidateJoinEligibilityCommand command);
}
