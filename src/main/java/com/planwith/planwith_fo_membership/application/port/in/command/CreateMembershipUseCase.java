package com.planwith.planwith_fo_membership.application.port.in.command;

import com.planwith.planwith_fo_membership.application.command.CreateMembershipCommand;

public interface CreateMembershipUseCase {

	void create(CreateMembershipCommand command);
}
