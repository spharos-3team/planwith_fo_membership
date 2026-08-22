package com.planwith.planwith_fo_membership.application.port.out;

import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;

public interface FollowQueryPort {

	boolean isFollowing(MemberUuid followerUuid, CreatorUuid creatorUuid);
}
