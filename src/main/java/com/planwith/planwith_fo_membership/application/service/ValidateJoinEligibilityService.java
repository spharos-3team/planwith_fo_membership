package com.planwith.planwith_fo_membership.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_membership.application.command.ValidateJoinEligibilityCommand;
import com.planwith.planwith_fo_membership.application.port.in.command.ValidateJoinEligibilityUseCase;
import com.planwith.planwith_fo_membership.application.port.out.FollowQueryPort;
import com.planwith.planwith_fo_membership.application.port.out.LoadMembershipPort;
import com.planwith.planwith_fo_membership.application.port.out.LoadSubscriptionPort;
import com.planwith.planwith_fo_membership.application.query.ValidateJoinEligibilityResult;
import com.planwith.planwith_fo_membership.domain.exception.MembershipNotFoundException;
import com.planwith.planwith_fo_membership.domain.model.Membership;
import com.planwith.planwith_fo_membership.domain.service.JoinEligibilityPolicy;
import com.planwith.planwith_fo_membership.domain.service.MembershipApplicationPolicy;

@Service
public class ValidateJoinEligibilityService implements ValidateJoinEligibilityUseCase {

	private static final Logger log = LoggerFactory.getLogger(ValidateJoinEligibilityService.class);

	private final LoadMembershipPort loadMembershipPort;
	private final FollowQueryPort followQueryPort;
	private final LoadSubscriptionPort loadSubscriptionPort;

	public ValidateJoinEligibilityService(
			LoadMembershipPort loadMembershipPort,
			FollowQueryPort followQueryPort,
			LoadSubscriptionPort loadSubscriptionPort
	) {
		this.loadMembershipPort = loadMembershipPort;
		this.followQueryPort = followQueryPort;
		this.loadSubscriptionPort = loadSubscriptionPort;
	}

	@Override
	@Transactional(readOnly = true)
	public ValidateJoinEligibilityResult validate(ValidateJoinEligibilityCommand command) {
		log.info(
				"ValidateJoinEligibilityService : validate : 가입 자격 검증 시작 - memberUuid={}, creatorUuid={}",
				command.memberUuid(),
				command.creatorUuid()
		);
		Membership membership = loadMembershipPort.findOpenByCreator(command.creatorUuid())
				.orElseThrow(() -> new MembershipNotFoundException("가입 가능한 멤버십이 없습니다."));
		JoinEligibilityPolicy.requireCanAcceptJoin(membership);
		boolean following = followQueryPort.isFollowing(command.memberUuid(), command.creatorUuid());
		log.debug(
				"ValidateJoinEligibilityService : validate : 팔로우 여부 확인 - memberUuid={}, creatorUuid={}, following={}",
				command.memberUuid(),
				command.creatorUuid(),
				following
		);
		JoinEligibilityPolicy.requireFollowing(following);
		boolean hasActiveSubscription = loadSubscriptionPort
				.findCurrentByMemberAndCreator(command.memberUuid(), command.creatorUuid())
				.isPresent();
		JoinEligibilityPolicy.requireNotDuplicate(hasActiveSubscription);
		log.info(
				"ValidateJoinEligibilityService : validate : 가입 자격 검증 완료 - memberUuid={}, creatorUuid={}, membershipUuid={}",
				command.memberUuid(),
				command.creatorUuid(),
				membership.membershipUuid()
		);
		return new ValidateJoinEligibilityResult(
				command.memberUuid(),
				command.creatorUuid(),
				membership.membershipUuid(),
				membership.monthlyPrice(),
				MembershipApplicationPolicy.PRICE_UNIT_TOKEN,
				true,
				true
		);
	}
}
