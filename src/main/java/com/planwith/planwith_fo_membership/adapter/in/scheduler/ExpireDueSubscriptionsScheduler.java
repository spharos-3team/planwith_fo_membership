package com.planwith.planwith_fo_membership.adapter.in.scheduler;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_membership.application.port.in.command.ExpireDueSubscriptionsUseCase;

@Component
@ConditionalOnProperty(name = "membership.expire.scheduler-enabled", havingValue = "true")
public class ExpireDueSubscriptionsScheduler {

	private static final Logger log = LoggerFactory.getLogger(ExpireDueSubscriptionsScheduler.class);

	private final ExpireDueSubscriptionsUseCase expireDueSubscriptionsUseCase;

	public ExpireDueSubscriptionsScheduler(ExpireDueSubscriptionsUseCase expireDueSubscriptionsUseCase) {
		this.expireDueSubscriptionsUseCase = expireDueSubscriptionsUseCase;
	}

	@Scheduled(
			fixedDelayString = "${membership.expire.interval:1h}",
			initialDelayString = "${membership.expire.initial-delay:1m}"
	)
	public void expireDueSubscriptions() {
		log.info("ExpireDueSubscriptionsScheduler : expireDueSubscriptions : 기간 만료 구독 배치 시작");
		int count = expireDueSubscriptionsUseCase.expireDue(Instant.now());
		log.info("ExpireDueSubscriptionsScheduler : expireDueSubscriptions : 기간 만료 구독 배치 완료 - count={}", count);
	}
}
