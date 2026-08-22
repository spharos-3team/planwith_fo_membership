package com.planwith.planwith_fo_membership.application.query;

import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;

public record ValidateMembershipApplicationResult(
		CreatorUuid creatorUuid,
		String gradeCode,
		int monthlyPrice,
		String priceUnit,
		boolean eligible
) {
}
