package com.planwith.planwith_fo_membership.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_membership.application.port.in.query.GetRevenueQueryUseCase;
import com.planwith.planwith_fo_membership.application.port.out.LoadRevenuePort;
import com.planwith.planwith_fo_membership.application.query.GetRevenueQuery;
import com.planwith.planwith_fo_membership.application.query.RevenueResult;

@Service
public class GetRevenueQueryService implements GetRevenueQueryUseCase {

	private static final Logger log = LoggerFactory.getLogger(GetRevenueQueryService.class);

	private final LoadRevenuePort loadRevenuePort;

	public GetRevenueQueryService(LoadRevenuePort loadRevenuePort) {
		this.loadRevenuePort = loadRevenuePort;
	}

	@Override
	@Transactional(readOnly = true)
	public RevenueResult get(GetRevenueQuery query) {
		RevenueResult result = loadRevenuePort.findByCreator(query.creatorUuid())
				.map(RevenueResult::from)
				.orElseGet(() -> RevenueResult.empty(query.creatorUuid()));
		log.debug(
				"GetRevenueQueryService : get : Creator 수익 조회 - creatorUuid={}, totalRevenue={}, availableRevenue={}, reservedRevenue={}, settledRevenue={}",
				query.creatorUuid(),
				result.totalRevenue(),
				result.availableRevenue(),
				result.reservedRevenue(),
				result.settledRevenue()
		);
		return result;
	}
}
