package com.planwith.planwith_fo_membership.adapter.out.follow;

import java.util.HashSet;
import java.util.Set;

import com.planwith.planwith_fo_membership.application.port.out.FollowQueryPort;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;

public class InMemoryFollowQueryAdapter implements FollowQueryPort {

	private final Set<String> followings = new HashSet<>();

	public void follow(MemberUuid followerUuid, CreatorUuid creatorUuid) {
		followings.add(key(followerUuid, creatorUuid));
	}

	public void clear() {
		followings.clear();
	}

	@Override
	public boolean isFollowing(MemberUuid followerUuid, CreatorUuid creatorUuid) {
		return followings.contains(key(followerUuid, creatorUuid));
	}

	private static String key(MemberUuid followerUuid, CreatorUuid creatorUuid) {
		return followerUuid + ":" + creatorUuid;
	}
}
