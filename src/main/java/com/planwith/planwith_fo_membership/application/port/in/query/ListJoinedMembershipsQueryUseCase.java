package com.planwith.planwith_fo_membership.application.port.in.query;

import java.util.List;

import com.planwith.planwith_fo_membership.application.query.JoinedMembershipResult;
import com.planwith.planwith_fo_membership.application.query.ListJoinedMembershipsQuery;

public interface ListJoinedMembershipsQueryUseCase {

	List<JoinedMembershipResult> list(ListJoinedMembershipsQuery query);
}
