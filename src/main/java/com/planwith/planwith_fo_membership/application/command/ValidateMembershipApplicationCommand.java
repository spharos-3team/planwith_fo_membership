package com.planwith.planwith_fo_membership.application.command;

import java.util.Objects;

import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;

public record ValidateMembershipApplicationCommand(
		CreatorUuid creatorUuid,
		String membershipName,
		String description,
		int monthlyPrice,
		String priceUnit
) {

	public ValidateMembershipApplicationCommand {
		Objects.requireNonNull(creatorUuid, "Creator UUID is required.");
		Objects.requireNonNull(membershipName, "Membership name is required.");
		Objects.requireNonNull(priceUnit, "Price unit is required.");
	}
}
