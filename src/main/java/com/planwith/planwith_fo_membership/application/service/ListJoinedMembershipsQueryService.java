package com.planwith.planwith_fo_membership.application.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_membership.application.port.in.query.ListJoinedMembershipsQueryUseCase;
import com.planwith.planwith_fo_membership.application.port.out.LoadSubscriptionPort;
import com.planwith.planwith_fo_membership.application.query.JoinedMembershipResult;
import com.planwith.planwith_fo_membership.application.query.ListJoinedMembershipsQuery;

@Service
public class ListJoinedMembershipsQueryService implements ListJoinedMembershipsQueryUseCase {

	private static final Logger log = LoggerFactory.getLogger(ListJoinedMembershipsQueryService.class);

	private final LoadSubscriptionPort loadSubscriptionPort;

	public ListJoinedMembershipsQueryService(LoadSubscriptionPort loadSubscriptionPort) {
		this.loadSubscriptionPort = loadSubscriptionPort;
	}

	@Override
	@Transactional(readOnly = true)
	public List<JoinedMembershipResult> list(ListJoinedMembershipsQuery query) {
		List<JoinedMembershipResult> result = loadSubscriptionPort.findJoinedByMember(query.memberUuid());
		log.debug(
				"ListJoinedMembershipsQueryService : list : 가입 멤버십 목록 조회 - memberUuid={}, count={}",
				query.memberUuid(),
				result.size()
		);
		return result;
	}
}
