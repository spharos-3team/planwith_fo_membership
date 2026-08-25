package com.planwith.planwith_fo_membership.application.command;

import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;

public record UpdateMembershipCommand(
		CreatorUuid creatorUuid,
		int monthlyPrice
) {
}
