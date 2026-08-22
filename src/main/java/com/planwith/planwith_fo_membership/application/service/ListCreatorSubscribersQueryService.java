package com.planwith.planwith_fo_membership.application.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_membership.application.port.in.query.ListCreatorSubscribersQueryUseCase;
import com.planwith.planwith_fo_membership.application.port.out.LoadSubscriptionPort;
import com.planwith.planwith_fo_membership.application.query.CreatorSubscriberResult;
import com.planwith.planwith_fo_membership.application.query.CreatorSubscribersResult;
import com.planwith.planwith_fo_membership.application.query.ListCreatorSubscribersQuery;

@Service
public class ListCreatorSubscribersQueryService implements ListCreatorSubscribersQueryUseCase {

	private static final Logger log = LoggerFactory.getLogger(ListCreatorSubscribersQueryService.class);

	private final LoadSubscriptionPort loadSubscriptionPort;

	public ListCreatorSubscribersQueryService(LoadSubscriptionPort loadSubscriptionPort) {
		this.loadSubscriptionPort = loadSubscriptionPort;
	}

	@Override
	@Transactional(readOnly = true)
	public CreatorSubscribersResult list(ListCreatorSubscribersQuery query) {
		List<CreatorSubscriberResult> subscribers = loadSubscriptionPort.findActiveByCreator(query.creatorUuid())
				.stream()
				.map(subscription -> new CreatorSubscriberResult(
						subscription.subscriptionUuid(),
						subscription.memberUuid(),
						subscription.startedAt()
				))
				.toList();
		log.debug(
				"ListCreatorSubscribersQueryService : list : Creator 가입자 조회 - creatorUuid={}, count={}",
				query.creatorUuid(),
				subscribers.size()
		);
		return new CreatorSubscribersResult(subscribers.size(), subscribers);
	}
}
