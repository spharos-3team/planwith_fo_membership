package com.planwith.planwith_fo_membership.domain.service;

import com.planwith.planwith_fo_membership.domain.exception.DuplicateSubscriptionException;
import com.planwith.planwith_fo_membership.domain.exception.FollowRequiredException;
import com.planwith.planwith_fo_membership.domain.exception.MembershipNotApprovedException;
import com.planwith.planwith_fo_membership.domain.model.Membership;

public final class JoinEligibilityPolicy {

	private JoinEligibilityPolicy() {
	}

	public static boolean canAcceptJoin(Membership membership) {
		return MembershipPolicy.canAcceptSubscription(membership);
	}

	public static boolean requiresFollow(boolean following) {
		return !following;
	}

	public static boolean isDuplicateSubscription(boolean hasActiveSubscription) {
		return hasActiveSubscription;
	}

	public static void requireCanAcceptJoin(Membership membership) {
		if (!canAcceptJoin(membership)) {
			throw new MembershipNotApprovedException("승인된 멤버십만 가입할 수 있습니다.");
		}
	}

	public static void requireFollowing(boolean following) {
		if (requiresFollow(following)) {
			throw new FollowRequiredException("팔로워만 멤버십에 가입할 수 있습니다.");
		}
	}

	public static void requireNotDuplicate(boolean hasActiveSubscription) {
		if (isDuplicateSubscription(hasActiveSubscription)) {
			throw new DuplicateSubscriptionException("이미 가입한 멤버십입니다.");
		}
	}
}
