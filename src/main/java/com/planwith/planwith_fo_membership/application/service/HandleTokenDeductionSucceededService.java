package com.planwith.planwith_fo_membership.application.service;

import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_membership.application.command.GrantEntitlementCommand;
import com.planwith.planwith_fo_membership.application.command.HandleTokenDeductionSucceededCommand;
import com.planwith.planwith_fo_membership.application.event.MembershipSubscribedEvent;
import com.planwith.planwith_fo_membership.application.port.in.command.GrantEntitlementUseCase;
import com.planwith.planwith_fo_membership.application.port.in.command.HandleTokenDeductionSucceededUseCase;
import com.planwith.planwith_fo_membership.application.port.out.LoadMembershipPort;
import com.planwith.planwith_fo_membership.application.port.out.LoadMembershipSagaPort;
import com.planwith.planwith_fo_membership.application.port.out.LoadPaymentPort;
import com.planwith.planwith_fo_membership.application.port.out.LoadRevenueLedgerPort;
import com.planwith.planwith_fo_membership.application.port.out.LoadRevenuePort;
import com.planwith.planwith_fo_membership.application.port.out.LoadSubscriptionPort;
import com.planwith.planwith_fo_membership.application.port.out.MembershipEventOutboxPort;
import com.planwith.planwith_fo_membership.application.port.out.MembershipOutboxMessage;
import com.planwith.planwith_fo_membership.application.port.out.ProcessedMembershipEventPort;
import com.planwith.planwith_fo_membership.application.port.out.SaveMembershipSagaPort;
import com.planwith.planwith_fo_membership.application.port.out.SavePaymentPort;
import com.planwith.planwith_fo_membership.application.port.out.SaveRevenueLedgerPort;
import com.planwith.planwith_fo_membership.application.port.out.SaveRevenuePort;
import com.planwith.planwith_fo_membership.application.port.out.SaveSubscriptionPort;
import com.planwith.planwith_fo_membership.domain.event.MembershipEventTypes;
import com.planwith.planwith_fo_membership.domain.exception.InvalidPaymentStateException;
import com.planwith.planwith_fo_membership.domain.exception.MembershipNotFoundException;
import com.planwith.planwith_fo_membership.domain.model.Membership;
import com.planwith.planwith_fo_membership.domain.model.MembershipPayment;
import com.planwith.planwith_fo_membership.domain.model.MembershipRevenue;
import com.planwith.planwith_fo_membership.domain.model.MembershipRevenueLedger;
import com.planwith.planwith_fo_membership.domain.model.MembershipSaga;
import com.planwith.planwith_fo_membership.domain.model.MembershipSubscription;
import com.planwith.planwith_fo_membership.domain.model.PaymentStatus;
import com.planwith.planwith_fo_membership.domain.model.ProcessedMembershipEvent;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.LedgerUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.RevenueUuid;
import com.planwith.planwith_fo_membership.domain.service.MembershipApplicationPolicy;
import com.planwith.planwith_fo_membership.domain.service.RevenueSharePolicy;
import com.planwith.planwith_fo_membership.domain.service.RevenueSharePolicy.RevenueShareResult;

@Service
public class HandleTokenDeductionSucceededService implements HandleTokenDeductionSucceededUseCase {

	private static final Logger log = LoggerFactory.getLogger(HandleTokenDeductionSucceededService.class);

	private final ProcessedMembershipEventPort processedMembershipEventPort;
	private final LoadPaymentPort loadPaymentPort;
	private final SavePaymentPort savePaymentPort;
	private final LoadMembershipSagaPort loadMembershipSagaPort;
	private final SaveMembershipSagaPort saveMembershipSagaPort;
	private final LoadMembershipPort loadMembershipPort;
	private final LoadSubscriptionPort loadSubscriptionPort;
	private final SaveSubscriptionPort saveSubscriptionPort;
	private final LoadRevenuePort loadRevenuePort;
	private final SaveRevenuePort saveRevenuePort;
	private final LoadRevenueLedgerPort loadRevenueLedgerPort;
	private final SaveRevenueLedgerPort saveRevenueLedgerPort;
	private final GrantEntitlementUseCase grantEntitlementUseCase;
	private final MembershipEventOutboxPort membershipEventOutboxPort;
	private final ObjectMapper objectMapper;

	public HandleTokenDeductionSucceededService(
			ProcessedMembershipEventPort processedMembershipEventPort,
			LoadPaymentPort loadPaymentPort,
			SavePaymentPort savePaymentPort,
			LoadMembershipSagaPort loadMembershipSagaPort,
			SaveMembershipSagaPort saveMembershipSagaPort,
			LoadMembershipPort loadMembershipPort,
			LoadSubscriptionPort loadSubscriptionPort,
			SaveSubscriptionPort saveSubscriptionPort,
			LoadRevenuePort loadRevenuePort,
			SaveRevenuePort saveRevenuePort,
			LoadRevenueLedgerPort loadRevenueLedgerPort,
			SaveRevenueLedgerPort saveRevenueLedgerPort,
			GrantEntitlementUseCase grantEntitlementUseCase,
			MembershipEventOutboxPort membershipEventOutboxPort,
			ObjectMapper objectMapper
	) {
		this.processedMembershipEventPort = processedMembershipEventPort;
		this.loadPaymentPort = loadPaymentPort;
		this.savePaymentPort = savePaymentPort;
		this.loadMembershipSagaPort = loadMembershipSagaPort;
		this.saveMembershipSagaPort = saveMembershipSagaPort;
		this.loadMembershipPort = loadMembershipPort;
		this.loadSubscriptionPort = loadSubscriptionPort;
		this.saveSubscriptionPort = saveSubscriptionPort;
		this.loadRevenuePort = loadRevenuePort;
		this.saveRevenuePort = saveRevenuePort;
		this.loadRevenueLedgerPort = loadRevenueLedgerPort;
		this.saveRevenueLedgerPort = saveRevenueLedgerPort;
		this.grantEntitlementUseCase = grantEntitlementUseCase;
		this.membershipEventOutboxPort = membershipEventOutboxPort;
		this.objectMapper = objectMapper;
	}

	@Override
	@Transactional
	public void handle(HandleTokenDeductionSucceededCommand command) {
		if (processedMembershipEventPort.existsByEventUuid(command.eventUuid())) {
			log.warn(
					"HandleTokenDeductionSucceededService : handle : 중복 TokenDeductionSucceeded 이벤트 무시 - eventUuid={}",
					command.eventUuid()
			);
			return;
		}
		MembershipPayment payment = loadPaymentPort.findByUuid(command.paymentUuid())
				.orElseThrow(() -> new InvalidPaymentStateException("결제 정보를 찾을 수 없습니다."));
		if (payment.paymentStatus() == PaymentStatus.SUCCESS
				&& loadSubscriptionPort.findByUuid(payment.subscriptionUuid()).isPresent()) {
			markProcessed(command);
			return;
		}
		if (payment.paymentStatus() != PaymentStatus.READY) {
			throw new InvalidPaymentStateException("대기 중인 결제만 토큰 차감 성공으로 확정할 수 있습니다.");
		}
		MembershipSaga saga = loadMembershipSagaPort.findByPaymentUuid(payment.paymentUuid())
				.orElseThrow(() -> new InvalidPaymentStateException("결제에 연결된 가입 Saga를 찾을 수 없습니다."));
		CreatorUuid creatorUuid = command.creatorUuid() == null ? saga.creatorUuid() : command.creatorUuid();
		Membership membership = loadMembershipPort.findOpenByCreator(creatorUuid)
				.orElseThrow(() -> new MembershipNotFoundException("가입 가능한 멤버십이 없습니다."));
		Instant completedAt = command.succeededAt() == null ? Instant.now() : command.succeededAt();

		MembershipPayment succeeded = payment.succeed(completedAt);
		savePaymentPort.save(succeeded);
		saveMembershipSagaPort.save(saga.paymentCompleted(completedAt).activated(completedAt));
		saveSubscriptionPort.save(MembershipSubscription.subscribe(
				membership,
				succeeded.subscriptionUuid(),
				command.memberUuid(),
				completedAt
		));
		recordRevenueShare(creatorUuid, succeeded, completedAt);
		grantEntitlementUseCase.grant(new GrantEntitlementCommand(
				command.memberUuid(),
				creatorUuid,
				succeeded.subscriptionUuid()
		));
		membershipEventOutboxPort.save(toSubscribedOutbox(command, creatorUuid, membership, succeeded, completedAt));
		markProcessed(command);
		log.info(
				"HandleTokenDeductionSucceededService : handle : 토큰 차감 성공으로 구독 활성화 - paymentUuid={}, subscriptionUuid={}",
				succeeded.paymentUuid(),
				succeeded.subscriptionUuid()
		);
	}

	private void recordRevenueShare(CreatorUuid creatorUuid, MembershipPayment succeeded, Instant recordedAt) {
		if (loadRevenueLedgerPort.findByPaymentUuid(succeeded.paymentUuid()).isPresent()) {
			log.warn(
					"HandleTokenDeductionSucceededService : handle : 동일 결제 수익 원장이 있어 배분을 생략한다 - paymentUuid={}",
					succeeded.paymentUuid()
			);
			return;
		}
		RevenueShareResult share = RevenueSharePolicy.split(succeeded.amount());
		log.debug(
				"HandleTokenDeductionSucceededService : handle : 수익 배분 계산 확인 - paymentUuid={}, tokenAmount={}, grossKrw={}, companyShareKrw={}, creatorShareKrw={}",
				succeeded.paymentUuid(),
				share.tokenAmount(),
				share.grossKrw(),
				share.companyShareKrw(),
				share.creatorShareKrw()
		);
		saveRevenueLedgerPort.save(MembershipRevenueLedger.recorded(
				new LedgerUuid(UUID.randomUUID()),
				succeeded.paymentUuid(),
				creatorUuid,
				share,
				recordedAt
		));
		saveRevenuePort.save(recordCreatorRevenue(creatorUuid, share.creatorShareKrw()));
		log.info(
				"HandleTokenDeductionSucceededService : handle : 수익 배분 기록 완료 - paymentUuid={}, grossKrw={}, companyShareKrw={}, creatorShareKrw={}",
				succeeded.paymentUuid(),
				share.grossKrw(),
				share.companyShareKrw(),
				share.creatorShareKrw()
		);
	}

	private MembershipRevenue recordCreatorRevenue(CreatorUuid creatorUuid, long creatorShareKrw) {
		return loadRevenuePort.findByCreator(creatorUuid)
				.orElseGet(() -> MembershipRevenue.empty(new RevenueUuid(UUID.randomUUID()), creatorUuid))
				.record(creatorShareKrw);
	}

	private MembershipOutboxMessage toSubscribedOutbox(
			HandleTokenDeductionSucceededCommand command,
			CreatorUuid creatorUuid,
			Membership membership,
			MembershipPayment payment,
			Instant subscribedAt
	) {
		UUID eventUuid = UUID.randomUUID();
		MembershipSubscribedEvent event = new MembershipSubscribedEvent(
				eventUuid.toString(),
				command.memberUuid().value().toString(),
				creatorUuid.value().toString(),
				membership.membershipUuid().value().toString(),
				payment.subscriptionUuid().value().toString(),
				payment.paymentUuid().value().toString(),
				payment.amount(),
				MembershipApplicationPolicy.PRICE_UNIT_TOKEN,
				subscribedAt
		);
		return new MembershipOutboxMessage(
				eventUuid.toString(),
				MembershipEventTypes.AGGREGATE_SUBSCRIPTION,
				payment.subscriptionUuid().value().toString(),
				MembershipEventTypes.MEMBERSHIP_SUBSCRIBED,
				writePayload(event),
				subscribedAt
		);
	}

	private void markProcessed(HandleTokenDeductionSucceededCommand command) {
		Instant processedAt = command.succeededAt() == null ? Instant.now() : command.succeededAt();
		processedMembershipEventPort.saveIdempotent(ProcessedMembershipEvent.recorded(
				command.eventUuid(),
				command.memberUuid(),
				MembershipEventTypes.TOKEN_DEDUCTION_SUCCEEDED,
				processedAt
		));
	}

	private String writePayload(MembershipSubscribedEvent event) {
		try {
			return objectMapper.writeValueAsString(event);
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("멤버십 가입 이벤트 직렬화에 실패했습니다.", exception);
		}
	}
}
