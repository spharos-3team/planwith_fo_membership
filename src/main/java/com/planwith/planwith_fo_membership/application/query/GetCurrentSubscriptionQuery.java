package com.planwith.planwith_fo_membership.application.query;

import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;

public record GetCurrentSubscriptionQuery(
		MemberUuid memberUuid,
		CreatorUuid creatorUuid
) {
}
