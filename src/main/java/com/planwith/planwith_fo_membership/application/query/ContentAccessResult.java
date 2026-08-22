package com.planwith.planwith_fo_membership.application.query;

import com.planwith.planwith_fo_membership.domain.model.Entitlement;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;

public record ContentAccessResult(
		MemberUuid memberUuid,
		CreatorUuid creatorUuid,
		boolean allowed,
		String status
) {

	public static ContentAccessResult from(Entitlement entitlement) {
		return new ContentAccessResult(
				entitlement.memberUuid(),
				entitlement.creatorUuid(),
				entitlement.allowed(),
				entitlement.status()
		);
	}
}
