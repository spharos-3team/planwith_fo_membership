package com.planwith.planwith_fo_membership.application.command;

import java.util.Objects;

import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;

public record StartTokenPaymentCommand(
		MemberUuid memberUuid,
		CreatorUuid creatorUuid
) {

	public StartTokenPaymentCommand {
		Objects.requireNonNull(memberUuid, "Member UUID is required.");
		Objects.requireNonNull(creatorUuid, "Creator UUID is required.");
	}
}
