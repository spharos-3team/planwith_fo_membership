package com.planwith.planwith_fo_membership.application.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_membership.application.port.in.query.GetCurrentSubscriptionQueryUseCase;
import com.planwith.planwith_fo_membership.application.port.out.LoadSubscriptionPort;
import com.planwith.planwith_fo_membership.application.query.CurrentSubscriptionResult;
import com.planwith.planwith_fo_membership.application.query.GetCurrentSubscriptionQuery;

@Service
public class GetCurrentSubscriptionQueryService implements GetCurrentSubscriptionQueryUseCase {

	private static final Logger log = LoggerFactory.getLogger(GetCurrentSubscriptionQueryService.class);

	private final LoadSubscriptionPort loadSubscriptionPort;

	public GetCurrentSubscriptionQueryService(LoadSubscriptionPort loadSubscriptionPort) {
		this.loadSubscriptionPort = loadSubscriptionPort;
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<CurrentSubscriptionResult> get(GetCurrentSubscriptionQuery query) {
		Optional<CurrentSubscriptionResult> result = loadSubscriptionPort
				.findCurrentByMemberAndCreator(query.memberUuid(), query.creatorUuid())
				.map(subscription -> new CurrentSubscriptionResult(
						subscription.subscriptionUuid(),
						subscription.membershipUuid(),
						subscription.memberUuid(),
						query.creatorUuid(),
						subscription.status(),
						subscription.startedAt()
				));
		log.debug(
				"GetCurrentSubscriptionQueryService : get : 현재 구독 조회 - memberUuid={}, creatorUuid={}, exists={}",
				query.memberUuid(),
				query.creatorUuid(),
				result.isPresent()
		);
		return result;
	}
}
