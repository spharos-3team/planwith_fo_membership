package com.planwith.planwith_fo_membership.application.port.in.query;

import com.planwith.planwith_fo_membership.application.query.CreatorSubscribersResult;
import com.planwith.planwith_fo_membership.application.query.ListCreatorSubscribersQuery;

public interface ListCreatorSubscribersQueryUseCase {

	CreatorSubscribersResult list(ListCreatorSubscribersQuery query);
}
