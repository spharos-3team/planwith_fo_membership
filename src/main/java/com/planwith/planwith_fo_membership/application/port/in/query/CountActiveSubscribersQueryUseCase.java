package com.planwith.planwith_fo_membership.application.port.in.query;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.planwith.planwith_fo_membership.application.query.CreatorSubscriberCountResult;

public interface CountActiveSubscribersQueryUseCase {

	List<CreatorSubscriberCountResult> count(Collection<UUID> creatorUuids);
}
