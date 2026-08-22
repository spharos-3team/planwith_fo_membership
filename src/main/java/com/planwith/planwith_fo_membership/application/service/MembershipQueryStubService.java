package com.planwith.planwith_fo_membership.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.planwith.planwith_fo_membership.application.port.in.query.GetSettlementQueryUseCase;
import com.planwith.planwith_fo_membership.application.query.GetSettlementQuery;
import com.planwith.planwith_fo_membership.domain.exception.UnsupportedMembershipOperationException;

@Service
public class MembershipQueryStubService implements GetSettlementQueryUseCase {

	private static final Logger log = LoggerFactory.getLogger(MembershipQueryStubService.class);

	@Override
	public void get(GetSettlementQuery query) {
		log.debug("MembershipQueryStubService : get : 정산 조회는 후속 이슈에서 구현한다");
		throw new UnsupportedMembershipOperationException("정산 조회는 후속 이슈에서 구현한다.");
	}
}
