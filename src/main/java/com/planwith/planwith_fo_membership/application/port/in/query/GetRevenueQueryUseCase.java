package com.planwith.planwith_fo_membership.application.port.in.query;

import com.planwith.planwith_fo_membership.application.query.GetRevenueQuery;
import com.planwith.planwith_fo_membership.application.query.RevenueResult;

public interface GetRevenueQueryUseCase {

	RevenueResult get(GetRevenueQuery query);
}
