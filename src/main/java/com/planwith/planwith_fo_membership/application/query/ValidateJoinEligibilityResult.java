package com.planwith.planwith_fo_membership.application.query;

import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;

public record ValidateJoinEligibilityResult(
		MemberUuid memberUuid,
		CreatorUuid creatorUuid,
		MembershipUuid membershipUuid,
		int monthlyPrice,
		String priceUnit,
		boolean following,
		boolean eligible
) {
}
