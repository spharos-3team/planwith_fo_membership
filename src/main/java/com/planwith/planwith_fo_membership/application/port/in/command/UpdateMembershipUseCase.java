package com.planwith.planwith_fo_membership.application.port.in.command;

import com.planwith.planwith_fo_membership.application.command.UpdateMembershipCommand;
import com.planwith.planwith_fo_membership.application.query.UpdateMembershipMonthlyPriceResult;

public interface UpdateMembershipUseCase {

	UpdateMembershipMonthlyPriceResult update(UpdateMembershipCommand command);
}
