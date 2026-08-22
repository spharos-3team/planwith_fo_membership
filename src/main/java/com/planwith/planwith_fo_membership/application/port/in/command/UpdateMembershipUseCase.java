package com.planwith.planwith_fo_membership.application.port.in.command;

import com.planwith.planwith_fo_membership.application.command.UpdateMembershipCommand;

public interface UpdateMembershipUseCase {

	void update(UpdateMembershipCommand command);
}
