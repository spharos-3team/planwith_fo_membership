package com.planwith.planwith_fo_membership.application.port.out;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.planwith.planwith_fo_membership.application.query.JoinedMembershipResult;
import com.planwith.planwith_fo_membership.domain.model.MembershipSubscription;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;

public interface LoadSubscriptionPort {

	Optional<MembershipSubscription> findByUuid(SubscriptionUuid subscriptionUuid);

	Optional<MembershipSubscription> findCurrentByMemberAndCreator(MemberUuid memberUuid, CreatorUuid creatorUuid);

	List<JoinedMembershipResult> findJoinedByMember(MemberUuid memberUuid);

	List<MembershipSubscription> findActiveByCreator(CreatorUuid creatorUuid);

	List<MembershipSubscription> findActiveStartedAtOnOrBefore(Instant startedAtOnOrBefore);
}
