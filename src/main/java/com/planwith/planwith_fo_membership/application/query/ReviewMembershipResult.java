package com.planwith.planwith_fo_membership.application.query;

import com.planwith.planwith_fo_membership.domain.model.MembershipStatus;
import com.planwith.planwith_fo_membership.domain.model.vo.AdminUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;

public record ReviewMembershipResult(
		MembershipUuid membershipUuid,
		CreatorUuid creatorUuid,
		MembershipStatus status,
		AdminUuid adminUuid,
		String rejectReason
) {
}
