package com.planwith.planwith_fo_membership.application.command;

import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;

public record CreateMembershipCommand(
		CreatorUuid creatorUuid,
		String title
) {
}
