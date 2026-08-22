package com.planwith.planwith_fo_membership.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_membership.application.port.in.query.CheckContentAccessQueryUseCase;
import com.planwith.planwith_fo_membership.application.port.out.EntitlementCachePort;
import com.planwith.planwith_fo_membership.application.port.out.LoadSubscriptionPort;
import com.planwith.planwith_fo_membership.application.query.CheckContentAccessQuery;
import com.planwith.planwith_fo_membership.application.query.ContentAccessResult;
import com.planwith.planwith_fo_membership.domain.model.Entitlement;
import com.planwith.planwith_fo_membership.domain.model.MembershipSubscription;

@Service
public class CheckContentAccessQueryService implements CheckContentAccessQueryUseCase {

	private static final Logger log = LoggerFactory.getLogger(CheckContentAccessQueryService.class);

	private final EntitlementCachePort entitlementCachePort;
	private final LoadSubscriptionPort loadSubscriptionPort;

	public CheckContentAccessQueryService(
			EntitlementCachePort entitlementCachePort,
			LoadSubscriptionPort loadSubscriptionPort
	) {
		this.entitlementCachePort = entitlementCachePort;
		this.loadSubscriptionPort = loadSubscriptionPort;
	}

	@Override
	@Transactional(readOnly = true)
	public ContentAccessResult check(CheckContentAccessQuery query) {
		return entitlementCachePort.find(query.memberUuid(), query.creatorUuid())
				.filter(Entitlement::allowed)
				.map(ContentAccessResult::from)
				.orElseGet(() -> loadFromSourceOfTruth(query));
	}

	private ContentAccessResult loadFromSourceOfTruth(CheckContentAccessQuery query) {
		log.debug(
				"CheckContentAccessQueryService : check : Entitlement 캐시 MISS로 Subscription 조회 - memberUuid={}, creatorUuid={}",
				query.memberUuid(),
				query.creatorUuid()
		);
		MembershipSubscription subscription = loadSubscriptionPort
				.findCurrentByMemberAndCreator(query.memberUuid(), query.creatorUuid())
				.orElse(null);
		Entitlement entitlement = subscription == null
				? Entitlement.denied(query.memberUuid(), query.creatorUuid())
				: Entitlement.from(subscription, query.creatorUuid());
		if (entitlement.allowed()) {
			entitlementCachePort.save(entitlement);
		} else {
			entitlementCachePort.evict(query.memberUuid(), query.creatorUuid());
		}
		return ContentAccessResult.from(entitlement);
	}
}
