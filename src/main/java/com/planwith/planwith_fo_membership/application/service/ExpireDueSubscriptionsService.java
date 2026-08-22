package com.planwith.planwith_fo_membership.application.service;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.planwith.planwith_fo_membership.application.command.ExpireSubscriptionCommand;
import com.planwith.planwith_fo_membership.application.port.in.command.ExpireDueSubscriptionsUseCase;
import com.planwith.planwith_fo_membership.application.port.in.command.ExpireSubscriptionUseCase;
import com.planwith.planwith_fo_membership.application.port.out.LoadSubscriptionPort;
import com.planwith.planwith_fo_membership.domain.service.SubscriptionPolicy;

@Service
public class ExpireDueSubscriptionsService implements ExpireDueSubscriptionsUseCase {

	private static final Logger log = LoggerFactory.getLogger(ExpireDueSubscriptionsService.class);

	private final LoadSubscriptionPort loadSubscriptionPort;
	private final ExpireSubscriptionUseCase expireSubscriptionUseCase;

	public ExpireDueSubscriptionsService(
			LoadSubscriptionPort loadSubscriptionPort,
			ExpireSubscriptionUseCase expireSubscriptionUseCase
	) {
		this.loadSubscriptionPort = loadSubscriptionPort;
		this.expireSubscriptionUseCase = expireSubscriptionUseCase;
	}

	@Override
	public int expireDue(Instant now) {
		Instant cutoff = now.minus(SubscriptionPolicy.DEFAULT_TERM);
		int expiredCount = 0;
		for (var subscription : loadSubscriptionPort.findActiveStartedAtOnOrBefore(cutoff)) {
			if (SubscriptionPolicy.isDueToExpire(subscription.startedAt(), now)) {
				expireSubscriptionUseCase.expire(new ExpireSubscriptionCommand(subscription.subscriptionUuid(), now));
				expiredCount++;
			}
		}
		log.info("ExpireDueSubscriptionsService : expireDue : 기간 만료 구독 처리 완료 - count={}", expiredCount);
		return expiredCount;
	}
}
