package com.planwith.planwith_fo_membership.application.service;

import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_membership.application.command.ExpireSubscriptionCommand;
import com.planwith.planwith_fo_membership.application.command.RevokeEntitlementCommand;
import com.planwith.planwith_fo_membership.application.event.MembershipExpiredEvent;
import com.planwith.planwith_fo_membership.application.port.in.command.ExpireSubscriptionUseCase;
import com.planwith.planwith_fo_membership.application.port.in.command.RevokeEntitlementUseCase;
import com.planwith.planwith_fo_membership.application.port.out.LoadMembershipPort;
import com.planwith.planwith_fo_membership.application.port.out.LoadSubscriptionPort;
import com.planwith.planwith_fo_membership.application.port.out.MembershipEventOutboxPort;
import com.planwith.planwith_fo_membership.application.port.out.MembershipOutboxMessage;
import com.planwith.planwith_fo_membership.application.port.out.SaveSubscriptionPort;
import com.planwith.planwith_fo_membership.domain.event.MembershipEventTypes;
import com.planwith.planwith_fo_membership.domain.exception.InvalidSubscriptionStateException;
import com.planwith.planwith_fo_membership.domain.exception.MembershipNotFoundException;
import com.planwith.planwith_fo_membership.domain.model.Membership;
import com.planwith.planwith_fo_membership.domain.model.MembershipSubscription;
import com.planwith.planwith_fo_membership.domain.service.SubscriptionPolicy;

@Service
public class ExpireSubscriptionService implements ExpireSubscriptionUseCase {

	private static final Logger log = LoggerFactory.getLogger(ExpireSubscriptionService.class);

	private final LoadSubscriptionPort loadSubscriptionPort;
	private final SaveSubscriptionPort saveSubscriptionPort;
	private final LoadMembershipPort loadMembershipPort;
	private final RevokeEntitlementUseCase revokeEntitlementUseCase;
	private final MembershipEventOutboxPort membershipEventOutboxPort;
	private final ObjectMapper objectMapper;

	public ExpireSubscriptionService(
			LoadSubscriptionPort loadSubscriptionPort,
			SaveSubscriptionPort saveSubscriptionPort,
			LoadMembershipPort loadMembershipPort,
			RevokeEntitlementUseCase revokeEntitlementUseCase,
			MembershipEventOutboxPort membershipEventOutboxPort,
			ObjectMapper objectMapper
	) {
		this.loadSubscriptionPort = loadSubscriptionPort;
		this.saveSubscriptionPort = saveSubscriptionPort;
		this.loadMembershipPort = loadMembershipPort;
		this.revokeEntitlementUseCase = revokeEntitlementUseCase;
		this.membershipEventOutboxPort = membershipEventOutboxPort;
		this.objectMapper = objectMapper;
	}

	@Override
	@Transactional
	public void expire(ExpireSubscriptionCommand command) {
		MembershipSubscription subscription = loadSubscriptionPort.findByUuid(command.subscriptionUuid())
				.orElseThrow(() -> new InvalidSubscriptionStateException("구독 정보를 찾을 수 없습니다."));
		if (!SubscriptionPolicy.canDeactivate(subscription.status())) {
			log.debug(
					"ExpireSubscriptionService : expire : 이미 비활성 구독이라 만료 처리를 생략한다 - subscriptionUuid={}",
					subscription.subscriptionUuid()
			);
			return;
		}
		Membership membership = loadMembershipPort.findByUuid(subscription.membershipUuid())
				.orElseThrow(() -> new MembershipNotFoundException("멤버십을 찾을 수 없습니다."));
		Instant expiredAt = command.expiredAt() == null ? Instant.now() : command.expiredAt();
		MembershipSubscription expired = subscription.deactivate(expiredAt);
		saveSubscriptionPort.save(expired);
		revokeEntitlementUseCase.revoke(new RevokeEntitlementCommand(
				expired.memberUuid(),
				membership.creatorUuid()
		));
		membershipEventOutboxPort.save(toExpiredOutbox(membership, expired, expiredAt));
		log.info(
				"ExpireSubscriptionService : expire : 구독 만료 완료 - subscriptionUuid={}, memberUuid={}, endedAt={}",
				expired.subscriptionUuid(),
				expired.memberUuid(),
				expired.endedAt()
		);
	}

	private MembershipOutboxMessage toExpiredOutbox(
			Membership membership,
			MembershipSubscription subscription,
			Instant expiredAt
	) {
		UUID eventUuid = UUID.randomUUID();
		MembershipExpiredEvent event = new MembershipExpiredEvent(
				eventUuid.toString(),
				subscription.memberUuid().value().toString(),
				membership.creatorUuid().value().toString(),
				membership.membershipUuid().value().toString(),
				subscription.subscriptionUuid().value().toString(),
				SubscriptionPolicy.PROCESSED_BY_SYSTEM,
				expiredAt
		);
		try {
			return new MembershipOutboxMessage(
					eventUuid.toString(),
					MembershipEventTypes.AGGREGATE_SUBSCRIPTION,
					subscription.subscriptionUuid().value().toString(),
					MembershipEventTypes.MEMBERSHIP_EXPIRED,
					objectMapper.writeValueAsString(event),
					expiredAt
			);
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("멤버십 만료 이벤트 직렬화에 실패했습니다.", exception);
		}
	}
}
