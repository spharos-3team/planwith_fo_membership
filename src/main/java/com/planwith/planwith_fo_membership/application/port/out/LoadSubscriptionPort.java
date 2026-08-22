package com.planwith.planwith_fo_membership.application.port.out;

import java.util.Optional;

import com.planwith.planwith_fo_membership.domain.model.Subscription;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;

public interface LoadSubscriptionPort {

	Optional<Subscription> findByUuid(SubscriptionUuid subscriptionUuid);

	Optional<Subscription> findCurrentByMemberAndCreator(MemberUuid memberUuid, CreatorUuid creatorUuid);
}
