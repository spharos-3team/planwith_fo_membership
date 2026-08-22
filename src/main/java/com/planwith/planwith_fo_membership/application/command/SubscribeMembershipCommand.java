package com.planwith.planwith_fo_membership.application.command;

import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;

public record SubscribeMembershipCommand(
		MemberUuid memberUuid,
		CreatorUuid creatorUuid
) {
}
