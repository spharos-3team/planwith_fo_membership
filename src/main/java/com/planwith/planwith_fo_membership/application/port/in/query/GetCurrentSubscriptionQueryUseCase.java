package com.planwith.planwith_fo_membership.application.port.in.query;

import java.util.Optional;

import com.planwith.planwith_fo_membership.application.query.CurrentSubscriptionResult;
import com.planwith.planwith_fo_membership.application.query.GetCurrentSubscriptionQuery;

public interface GetCurrentSubscriptionQueryUseCase {

	Optional<CurrentSubscriptionResult> get(GetCurrentSubscriptionQuery query);
}
