package com.planwith.planwith_fo_membership.application.query;

import java.util.Objects;

import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;

public record GetRevenueQuery(
		CreatorUuid creatorUuid
) {

	public GetRevenueQuery {
		Objects.requireNonNull(creatorUuid, "Creator UUID is required.");
	}
}
