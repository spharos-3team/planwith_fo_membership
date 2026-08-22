package com.planwith.planwith_fo_membership.application.port.in.query;

import com.planwith.planwith_fo_membership.application.query.CheckContentAccessQuery;
import com.planwith.planwith_fo_membership.application.query.ContentAccessResult;

public interface CheckContentAccessQueryUseCase {

	ContentAccessResult check(CheckContentAccessQuery query);
}
