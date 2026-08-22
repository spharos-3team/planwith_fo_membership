package com.planwith.planwith_fo_membership.application.port.out;

import com.planwith.planwith_fo_membership.application.command.TokenDeductionCommand;
import com.planwith.planwith_fo_membership.application.query.TokenDeductionResult;

public interface TokenCommandPort {

	TokenDeductionResult requestTokenDeduction(TokenDeductionCommand command);
}
