package com.planwith.planwith_fo_membership.application.command;

import java.util.Objects;

import com.planwith.planwith_fo_membership.domain.model.vo.AdminUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;

public record ApproveMembershipCommand(
		MembershipUuid membershipUuid,
		AdminUuid adminUuid
) {

	public ApproveMembershipCommand {
		Objects.requireNonNull(membershipUuid, "Membership UUID is required.");
		Objects.requireNonNull(adminUuid, "Admin UUID is required.");
	}
}
