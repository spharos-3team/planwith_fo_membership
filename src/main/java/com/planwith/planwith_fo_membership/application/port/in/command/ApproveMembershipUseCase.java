package com.planwith.planwith_fo_membership.application.port.in.command;

import com.planwith.planwith_fo_membership.application.command.ApproveMembershipCommand;
import com.planwith.planwith_fo_membership.application.query.ReviewMembershipResult;

public interface ApproveMembershipUseCase {

	ReviewMembershipResult approve(ApproveMembershipCommand command);
}
