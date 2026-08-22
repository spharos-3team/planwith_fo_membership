package com.planwith.planwith_fo_membership.application.command;

import java.time.Instant;
import java.util.Objects;

import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;

public record RequestSettlementCommand(
		CreatorUuid creatorUuid,
		long settlementAmount,
		Instant requestedAt
) {

	public RequestSettlementCommand {
		Objects.requireNonNull(creatorUuid, "Creator UUID is required.");
	}
}
