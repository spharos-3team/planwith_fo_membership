package com.planwith.planwith_fo_membership.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.planwith.planwith_fo_membership.application.port.in.query.GetCurrentSubscriptionQueryUseCase;
import com.planwith.planwith_fo_membership.application.port.in.query.GetRevenueQueryUseCase;
import com.planwith.planwith_fo_membership.application.port.in.query.GetSettlementQueryUseCase;
import com.planwith.planwith_fo_membership.application.port.in.query.ListCreatorSubscribersQueryUseCase;
import com.planwith.planwith_fo_membership.application.query.GetCurrentSubscriptionQuery;
import com.planwith.planwith_fo_membership.application.query.GetRevenueQuery;
import com.planwith.planwith_fo_membership.application.query.GetSettlementQuery;
import com.planwith.planwith_fo_membership.application.query.ListCreatorSubscribersQuery;
import com.planwith.planwith_fo_membership.domain.exception.UnsupportedMembershipOperationException;

@Service
public class MembershipQueryStubService implements
		GetCurrentSubscriptionQueryUseCase,
		ListCreatorSubscribersQueryUseCase,
		GetRevenueQueryUseCase,
		GetSettlementQueryUseCase {

	private static final Logger log = LoggerFactory.getLogger(MembershipQueryStubService.class);

	@Override
	public void get(GetCurrentSubscriptionQuery query) {
		log.debug("MembershipQueryStubService : get : 현재 구독 조회는 후속 이슈에서 구현한다");
		throw new UnsupportedMembershipOperationException("현재 구독 조회는 후속 이슈에서 구현한다.");
	}

	@Override
	public void list(ListCreatorSubscribersQuery query) {
		log.debug("MembershipQueryStubService : list : Creator 구독자 조회는 후속 이슈에서 구현한다");
		throw new UnsupportedMembershipOperationException("Creator 구독자 조회는 후속 이슈에서 구현한다.");
	}

	@Override
	public void get(GetRevenueQuery query) {
		log.debug("MembershipQueryStubService : get : 매출 조회는 후속 이슈에서 구현한다");
		throw new UnsupportedMembershipOperationException("매출 조회는 후속 이슈에서 구현한다.");
	}

	@Override
	public void get(GetSettlementQuery query) {
		log.debug("MembershipQueryStubService : get : 정산 조회는 후속 이슈에서 구현한다");
		throw new UnsupportedMembershipOperationException("정산 조회는 후속 이슈에서 구현한다.");
	}
}
