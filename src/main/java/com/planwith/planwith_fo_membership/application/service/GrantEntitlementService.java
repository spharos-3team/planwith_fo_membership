package com.planwith.planwith_fo_membership.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.planwith.planwith_fo_membership.application.command.GrantEntitlementCommand;
import com.planwith.planwith_fo_membership.application.port.in.command.GrantEntitlementUseCase;
import com.planwith.planwith_fo_membership.application.port.out.EntitlementCachePort;
import com.planwith.planwith_fo_membership.application.port.out.LoadSubscriptionPort;
import com.planwith.planwith_fo_membership.domain.model.Entitlement;
import com.planwith.planwith_fo_membership.domain.model.MembershipSubscription;
import com.planwith.planwith_fo_membership.domain.service.EntitlementPolicy;

@Service
public class GrantEntitlementService implements GrantEntitlementUseCase {

	private static final Logger log = LoggerFactory.getLogger(GrantEntitlementService.class);

	private final LoadSubscriptionPort loadSubscriptionPort;
	private final EntitlementCachePort entitlementCachePort;

	public GrantEntitlementService(
			LoadSubscriptionPort loadSubscriptionPort,
			EntitlementCachePort entitlementCachePort
	) {
		this.loadSubscriptionPort = loadSubscriptionPort;
		this.entitlementCachePort = entitlementCachePort;
	}

	@Override
	@Transactional
	public void grant(GrantEntitlementCommand command) {
		MembershipSubscription subscription = loadSubscriptionPort.findByUuid(command.subscriptionUuid())
				.orElse(null);
		if (!EntitlementPolicy.canGrant(subscription)
				|| !subscription.memberUuid().equals(command.memberUuid())) {
			entitlementCachePort.evict(command.memberUuid(), command.creatorUuid());
			log.warn(
					"GrantEntitlementService : grant : 활성 구독이 없어 권한을 부여하지 않는다 - memberUuid={}, creatorUuid={}, subscriptionUuid={}",
					command.memberUuid(),
					command.creatorUuid(),
					command.subscriptionUuid()
			);
			return;
		}
		if (entitlementCachePort.find(command.memberUuid(), command.creatorUuid())
				.filter(Entitlement::allowed)
				.isPresent()) {
			log.debug(
					"GrantEntitlementService : grant : 동일 구독 권한이 이미 부여되어 생략한다 - memberUuid={}, creatorUuid={}, subscriptionUuid={}",
					command.memberUuid(),
					command.creatorUuid(),
					command.subscriptionUuid()
			);
			return;
		}
		Entitlement entitlement = Entitlement.grant(subscription, command.creatorUuid());
		saveAfterCommit(entitlement);
	}

	private void saveAfterCommit(Entitlement entitlement) {
		Runnable save = () -> {
			entitlementCachePort.save(entitlement);
			log.info(
					"GrantEntitlementService : grant : 멤버십 접근 권한 부여 - memberUuid={}, creatorUuid={}, subscriptionUuid={}",
					entitlement.memberUuid(),
					entitlement.creatorUuid(),
					entitlement.subscriptionUuid()
			);
		};
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					save.run();
				}
			});
			return;
		}
		save.run();
	}
}
