package com.planwith.planwith_fo_membership.application.query;

import com.planwith.planwith_fo_membership.domain.model.MembershipStatus;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;

public record MyMembershipResult(
		MembershipUuid membershipUuid,
		CreatorUuid creatorUuid,
		String membershipName,
		int monthlyPrice,
		String priceUnit,
		MembershipStatus status,
		String rejectReason
) {
}
