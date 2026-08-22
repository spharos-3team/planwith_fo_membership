package com.planwith.planwith_fo_membership.adapter.out.follow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_membership.application.port.out.FollowQueryPort;
import com.planwith.planwith_fo_membership.domain.exception.UnsupportedMembershipOperationException;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;

@Component
public class StubFollowQueryAdapter implements FollowQueryPort {

	private static final Logger log = LoggerFactory.getLogger(StubFollowQueryAdapter.class);

	@Override
	public boolean isFollowing(MemberUuid followerUuid, CreatorUuid creatorUuid) {
		log.debug(
				"StubFollowQueryAdapter : isFollowing : Follow 서비스 조회는 후속 이슈에서 구현한다 - followerUuid={}, creatorUuid={}",
				followerUuid,
				creatorUuid
		);
		throw new UnsupportedMembershipOperationException("Follow 서비스 팔로우 조회는 후속 이슈에서 구현한다.");
	}
}
