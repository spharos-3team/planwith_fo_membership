package com.planwith.planwith_fo_membership.application.query;

import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;

public record ContentAccessResult(
		MemberUuid memberUuid,
		CreatorUuid creatorUuid,
		boolean allowed
) {
}
