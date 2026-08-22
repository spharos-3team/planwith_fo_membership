package com.planwith.planwith_fo_membership.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.planwith.planwith_fo_membership.application.command.RevokeEntitlementCommand;
import com.planwith.planwith_fo_membership.application.port.in.command.RevokeEntitlementUseCase;
import com.planwith.planwith_fo_membership.application.port.out.EntitlementCachePort;

@Service
public class RevokeEntitlementService implements RevokeEntitlementUseCase {

	private static final Logger log = LoggerFactory.getLogger(RevokeEntitlementService.class);

	private final EntitlementCachePort entitlementCachePort;

	public RevokeEntitlementService(EntitlementCachePort entitlementCachePort) {
		this.entitlementCachePort = entitlementCachePort;
	}

	@Override
	public void revoke(RevokeEntitlementCommand command) {
		entitlementCachePort.evict(command.memberUuid(), command.creatorUuid());
		log.info(
				"RevokeEntitlementService : revoke : 멤버십 접근 권한 회수 - memberUuid={}, creatorUuid={}",
				command.memberUuid(),
				command.creatorUuid()
		);
	}
}
