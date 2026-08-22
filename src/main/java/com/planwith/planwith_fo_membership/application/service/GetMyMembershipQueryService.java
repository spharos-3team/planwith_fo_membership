package com.planwith.planwith_fo_membership.application.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_membership.application.port.in.query.GetMyMembershipQueryUseCase;
import com.planwith.planwith_fo_membership.application.port.out.LoadMembershipPort;
import com.planwith.planwith_fo_membership.application.query.GetMyMembershipQuery;
import com.planwith.planwith_fo_membership.application.query.MyMembershipResult;
import com.planwith.planwith_fo_membership.domain.service.MembershipApplicationPolicy;

@Service
public class GetMyMembershipQueryService implements GetMyMembershipQueryUseCase {

	private static final Logger log = LoggerFactory.getLogger(GetMyMembershipQueryService.class);

	private final LoadMembershipPort loadMembershipPort;

	public GetMyMembershipQueryService(LoadMembershipPort loadMembershipPort) {
		this.loadMembershipPort = loadMembershipPort;
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<MyMembershipResult> get(GetMyMembershipQuery query) {
		Optional<MyMembershipResult> result = loadMembershipPort.findLatestByCreator(query.creatorUuid())
				.map(membership -> new MyMembershipResult(
						membership.membershipUuid(),
						membership.creatorUuid(),
						membership.membershipName(),
						membership.monthlyPrice(),
						MembershipApplicationPolicy.PRICE_UNIT_TOKEN,
						membership.status(),
						membership.rejectReason()
				));
		log.debug(
				"GetMyMembershipQueryService : get : 본인 멤버십 조회 - creatorUuid={}, exists={}",
				query.creatorUuid(),
				result.isPresent()
		);
		return result;
	}
}
