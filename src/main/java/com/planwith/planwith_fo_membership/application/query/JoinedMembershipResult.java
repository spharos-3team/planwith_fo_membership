package com.planwith.planwith_fo_membership.application.query;

import java.time.Instant;

import com.planwith.planwith_fo_membership.domain.model.SubscriptionStatus;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;

public record JoinedMembershipResult(
		SubscriptionUuid subscriptionUuid,
		MembershipUuid membershipUuid,
		CreatorUuid creatorUuid,
		String membershipName,
		int monthlyPrice,
		String priceUnit,
		SubscriptionStatus status,
		Instant startedAt
) {
}
