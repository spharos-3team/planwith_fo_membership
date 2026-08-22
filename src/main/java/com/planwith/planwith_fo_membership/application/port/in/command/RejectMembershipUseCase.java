package com.planwith.planwith_fo_membership.application.port.in.command;

import com.planwith.planwith_fo_membership.application.command.RejectMembershipCommand;
import com.planwith.planwith_fo_membership.application.query.ReviewMembershipResult;

public interface RejectMembershipUseCase {

	ReviewMembershipResult reject(RejectMembershipCommand command);
}
