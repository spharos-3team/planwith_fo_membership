package com.planwith.planwith_fo_membership.application.port.in.query;

import java.util.Optional;

import com.planwith.planwith_fo_membership.application.query.GetMyMembershipQuery;
import com.planwith.planwith_fo_membership.application.query.MyMembershipResult;

public interface GetMyMembershipQueryUseCase {

	Optional<MyMembershipResult> get(GetMyMembershipQuery query);
}
