package com.planwith.planwith_fo_membership.application.service;

import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_membership.application.command.CancelSubscriptionCommand;
import com.planwith.planwith_fo_membership.application.command.RevokeEntitlementCommand;
import com.planwith.planwith_fo_membership.application.event.MembershipCanceledEvent;
import com.planwith.planwith_fo_membership.application.port.in.command.CancelSubscriptionUseCase;
import com.planwith.planwith_fo_membership.application.port.in.command.RevokeEntitlementUseCase;
import com.planwith.planwith_fo_membership.application.port.out.LoadMembershipPort;
import com.planwith.planwith_fo_membership.application.port.out.LoadSubscriptionPort;
import com.planwith.planwith_fo_membership.application.port.out.MembershipEventOutboxPort;
import com.planwith.planwith_fo_membership.application.port.out.MembershipOutboxMessage;
import com.planwith.planwith_fo_membership.application.port.out.SaveSubscriptionPort;
import com.planwith.planwith_fo_membership.application.query.CancelSubscriptionResult;
import com.planwith.planwith_fo_membership.domain.event.MembershipEventTypes;
import com.planwith.planwith_fo_membership.domain.exception.InvalidSubscriptionStateException;
import com.planwith.planwith_fo_membership.domain.exception.MembershipNotFoundException;
import com.planwith.planwith_fo_membership.domain.model.Membership;
import com.planwith.planwith_fo_membership.domain.model.MembershipSubscription;
import com.planwith.planwith_fo_membership.domain.service.AccessPolicy;
import com.planwith.planwith_fo_membership.domain.service.SubscriptionPolicy;

@Service
public class CancelSubscriptionService implements CancelSubscriptionUseCase {

	private static final Logger log = LoggerFactory.getLogger(CancelSubscriptionService.class);

	private final LoadSubscriptionPort loadSubscriptionPort;
	private final SaveSubscriptionPort saveSubscriptionPort;
	private final LoadMembershipPort loadMembershipPort;
	private final RevokeEntitlementUseCase revokeEntitlementUseCase;
	private final MembershipEventOutboxPort membershipEventOutboxPort;
	private final ObjectMapper objectMapper;

	public CancelSubscriptionService(
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
	public CancelSubscriptionResult cancel(CancelSubscriptionCommand command) {
		MembershipSubscription subscription = loadSubscriptionPort.findByUuid(command.subscriptionUuid())
				.orElseThrow(() -> new InvalidSubscriptionStateException("구독 정보를 찾을 수 없습니다."));
		AccessPolicy.requireMember(command.memberUuid(), subscription.memberUuid(), "본인 구독만 해지할 수 있습니다.");
		SubscriptionPolicy.requireCanDeactivate(subscription.status());
		Membership membership = loadMembershipPort.findByUuid(subscription.membershipUuid())
				.orElseThrow(() -> new MembershipNotFoundException("멤버십을 찾을 수 없습니다."));
		Instant canceledAt = command.canceledAt() == null ? Instant.now() : command.canceledAt();
		MembershipSubscription canceled = subscription.deactivate(canceledAt);
		saveSubscriptionPort.save(canceled);
		revokeEntitlementUseCase.revoke(new RevokeEntitlementCommand(
				canceled.memberUuid(),
				membership.creatorUuid()
		));
		membershipEventOutboxPort.save(toCanceledOutbox(membership, canceled, command.memberUuid().toString(), canceledAt));
		log.info(
				"CancelSubscriptionService : cancel : 구독 해지 완료 - subscriptionUuid={}, memberUuid={}, endedAt={}",
				canceled.subscriptionUuid(),
				canceled.memberUuid(),
				canceled.endedAt()
		);
		return new CancelSubscriptionResult(canceled.subscriptionUuid(), canceled.status(), canceled.endedAt());
	}

	private MembershipOutboxMessage toCanceledOutbox(
			Membership membership,
			MembershipSubscription subscription,
			String processedBy,
			Instant canceledAt
	) {
		UUID eventUuid = UUID.randomUUID();
		MembershipCanceledEvent event = new MembershipCanceledEvent(
				eventUuid.toString(),
				subscription.memberUuid().value().toString(),
				membership.creatorUuid().value().toString(),
				membership.membershipUuid().value().toString(),
				subscription.subscriptionUuid().value().toString(),
				processedBy,
				canceledAt
		);
		try {
			return new MembershipOutboxMessage(
					eventUuid.toString(),
					MembershipEventTypes.AGGREGATE_SUBSCRIPTION,
					subscription.subscriptionUuid().value().toString(),
					MembershipEventTypes.MEMBERSHIP_CANCELED,
					objectMapper.writeValueAsString(event),
					canceledAt
			);
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("멤버십 해지 이벤트 직렬화에 실패했습니다.", exception);
		}
	}
}
