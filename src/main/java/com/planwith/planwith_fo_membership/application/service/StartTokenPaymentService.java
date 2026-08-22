package com.planwith.planwith_fo_membership.application.service;

import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_membership.application.command.StartTokenPaymentCommand;
import com.planwith.planwith_fo_membership.application.command.ValidateJoinEligibilityCommand;
import com.planwith.planwith_fo_membership.application.event.TokenDeductionRequestedEvent;
import com.planwith.planwith_fo_membership.application.port.in.command.StartTokenPaymentUseCase;
import com.planwith.planwith_fo_membership.application.port.in.command.ValidateJoinEligibilityUseCase;
import com.planwith.planwith_fo_membership.application.port.out.LoadMembershipSagaPort;
import com.planwith.planwith_fo_membership.application.port.out.LoadPaymentPort;
import com.planwith.planwith_fo_membership.application.port.out.MembershipEventOutboxPort;
import com.planwith.planwith_fo_membership.application.port.out.MembershipOutboxMessage;
import com.planwith.planwith_fo_membership.application.port.out.SaveMembershipSagaPort;
import com.planwith.planwith_fo_membership.application.port.out.SavePaymentPort;
import com.planwith.planwith_fo_membership.application.query.StartTokenPaymentResult;
import com.planwith.planwith_fo_membership.application.query.ValidateJoinEligibilityResult;
import com.planwith.planwith_fo_membership.domain.event.MembershipEventTypes;
import com.planwith.planwith_fo_membership.domain.model.MembershipPayment;
import com.planwith.planwith_fo_membership.domain.model.MembershipSaga;
import com.planwith.planwith_fo_membership.domain.model.PaymentStatus;
import com.planwith.planwith_fo_membership.domain.model.vo.PaymentUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;
import com.planwith.planwith_fo_membership.domain.service.MembershipApplicationPolicy;

@Service
public class StartTokenPaymentService implements StartTokenPaymentUseCase {

	private static final Logger log = LoggerFactory.getLogger(StartTokenPaymentService.class);
	private static final String REFERENCE_TYPE = "MEMBERSHIP_SUBSCRIPTION";

	private final ValidateJoinEligibilityUseCase validateJoinEligibilityUseCase;
	private final LoadMembershipSagaPort loadMembershipSagaPort;
	private final LoadPaymentPort loadPaymentPort;
	private final SavePaymentPort savePaymentPort;
	private final SaveMembershipSagaPort saveMembershipSagaPort;
	private final MembershipEventOutboxPort membershipEventOutboxPort;
	private final ObjectMapper objectMapper;

	public StartTokenPaymentService(
			ValidateJoinEligibilityUseCase validateJoinEligibilityUseCase,
			LoadMembershipSagaPort loadMembershipSagaPort,
			LoadPaymentPort loadPaymentPort,
			SavePaymentPort savePaymentPort,
			SaveMembershipSagaPort saveMembershipSagaPort,
			MembershipEventOutboxPort membershipEventOutboxPort,
			ObjectMapper objectMapper
	) {
		this.validateJoinEligibilityUseCase = validateJoinEligibilityUseCase;
		this.loadMembershipSagaPort = loadMembershipSagaPort;
		this.loadPaymentPort = loadPaymentPort;
		this.savePaymentPort = savePaymentPort;
		this.saveMembershipSagaPort = saveMembershipSagaPort;
		this.membershipEventOutboxPort = membershipEventOutboxPort;
		this.objectMapper = objectMapper;
	}

	@Override
	@Transactional
	public StartTokenPaymentResult start(StartTokenPaymentCommand command) {
		log.info(
				"StartTokenPaymentService : start : 토큰 결제 시작 요청 - memberUuid={}, creatorUuid={}",
				command.memberUuid(),
				command.creatorUuid()
		);
		ValidateJoinEligibilityResult eligibility = validateJoinEligibilityUseCase.validate(
				new ValidateJoinEligibilityCommand(command.memberUuid(), command.creatorUuid())
		);
		StartTokenPaymentResult existing = findExistingReadyPayment(command);
		if (existing != null) {
			log.info(
					"StartTokenPaymentService : start : 동일 가입 결제 READY 재사용 - memberUuid={}, paymentUuid={}",
					command.memberUuid(),
					existing.paymentUuid()
			);
			return existing;
		}

		Instant now = Instant.now();
		SubscriptionUuid subscriptionUuid = new SubscriptionUuid(UUID.randomUUID());
		PaymentUuid paymentUuid = new PaymentUuid(UUID.randomUUID());
		long amount = eligibility.monthlyPrice();
		MembershipPayment payment = MembershipPayment.ready(paymentUuid, subscriptionUuid, amount);
		MembershipSaga saga = MembershipSaga.subscribeRequested(
						UUID.randomUUID(),
						command.memberUuid(),
						command.creatorUuid(),
						subscriptionUuid,
						now
				)
				.paymentPending(paymentUuid, now);

		savePaymentPort.save(payment);
		saveMembershipSagaPort.save(saga);
		membershipEventOutboxPort.save(toOutboxMessage(command, payment, now));
		log.info(
				"StartTokenPaymentService : start : 토큰 결제 READY 및 Outbox 저장 완료 - memberUuid={}, paymentUuid={}, subscriptionUuid={}",
				command.memberUuid(),
				paymentUuid,
				subscriptionUuid
		);
		return new StartTokenPaymentResult(
				paymentUuid,
				subscriptionUuid,
				amount,
				MembershipApplicationPolicy.PRICE_UNIT_TOKEN,
				PaymentStatus.READY
		);
	}

	private StartTokenPaymentResult findExistingReadyPayment(StartTokenPaymentCommand command) {
		return loadMembershipSagaPort.findInProgressByMemberAndCreator(command.memberUuid(), command.creatorUuid())
				.filter(saga -> saga.paymentUuid() != null)
				.flatMap(saga -> loadPaymentPort.findByUuid(saga.paymentUuid()))
				.filter(payment -> payment.paymentStatus() == PaymentStatus.READY)
				.map(payment -> new StartTokenPaymentResult(
						payment.paymentUuid(),
						payment.subscriptionUuid(),
						payment.amount(),
						MembershipApplicationPolicy.PRICE_UNIT_TOKEN,
						payment.paymentStatus()
				))
				.orElse(null);
	}

	private MembershipOutboxMessage toOutboxMessage(
			StartTokenPaymentCommand command,
			MembershipPayment payment,
			Instant requestedAt
	) {
		TokenDeductionRequestedEvent event = new TokenDeductionRequestedEvent(
				payment.paymentUuid().value().toString(),
				command.memberUuid().value().toString(),
				command.creatorUuid().value().toString(),
				payment.paymentUuid().value().toString(),
				payment.subscriptionUuid().value().toString(),
				payment.amount(),
				MembershipApplicationPolicy.PRICE_UNIT_TOKEN,
				REFERENCE_TYPE,
				requestedAt
		);
		return new MembershipOutboxMessage(
				event.eventUuid(),
				MembershipEventTypes.AGGREGATE_PAYMENT,
				payment.paymentUuid().value().toString(),
				MembershipEventTypes.TOKEN_DEDUCTION_REQUESTED,
				writePayload(event),
				requestedAt
		);
	}

	private String writePayload(TokenDeductionRequestedEvent event) {
		try {
			return objectMapper.writeValueAsString(event);
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("토큰 차감 요청 이벤트 직렬화에 실패했습니다.", exception);
		}
	}
}
