package com.planwith.planwith_fo_membership.application.service;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_membership.application.command.HandleTokenDeductionFailedCommand;
import com.planwith.planwith_fo_membership.application.port.in.command.HandleTokenDeductionFailedUseCase;
import com.planwith.planwith_fo_membership.application.port.out.LoadMembershipSagaPort;
import com.planwith.planwith_fo_membership.application.port.out.LoadPaymentPort;
import com.planwith.planwith_fo_membership.application.port.out.LoadSubscriptionPort;
import com.planwith.planwith_fo_membership.application.port.out.ProcessedMembershipEventPort;
import com.planwith.planwith_fo_membership.application.port.out.SaveMembershipSagaPort;
import com.planwith.planwith_fo_membership.application.port.out.SavePaymentPort;
import com.planwith.planwith_fo_membership.domain.event.MembershipEventTypes;
import com.planwith.planwith_fo_membership.domain.exception.InvalidPaymentStateException;
import com.planwith.planwith_fo_membership.domain.model.MembershipPayment;
import com.planwith.planwith_fo_membership.domain.model.PaymentStatus;
import com.planwith.planwith_fo_membership.domain.model.ProcessedMembershipEvent;

@Service
public class HandleTokenDeductionFailedService implements HandleTokenDeductionFailedUseCase {

	private static final Logger log = LoggerFactory.getLogger(HandleTokenDeductionFailedService.class);

	private final ProcessedMembershipEventPort processedMembershipEventPort;
	private final LoadPaymentPort loadPaymentPort;
	private final SavePaymentPort savePaymentPort;
	private final LoadMembershipSagaPort loadMembershipSagaPort;
	private final SaveMembershipSagaPort saveMembershipSagaPort;
	private final LoadSubscriptionPort loadSubscriptionPort;

	public HandleTokenDeductionFailedService(
			ProcessedMembershipEventPort processedMembershipEventPort,
			LoadPaymentPort loadPaymentPort,
			SavePaymentPort savePaymentPort,
			LoadMembershipSagaPort loadMembershipSagaPort,
			SaveMembershipSagaPort saveMembershipSagaPort,
			LoadSubscriptionPort loadSubscriptionPort
	) {
		this.processedMembershipEventPort = processedMembershipEventPort;
		this.loadPaymentPort = loadPaymentPort;
		this.savePaymentPort = savePaymentPort;
		this.loadMembershipSagaPort = loadMembershipSagaPort;
		this.saveMembershipSagaPort = saveMembershipSagaPort;
		this.loadSubscriptionPort = loadSubscriptionPort;
	}

	@Override
	@Transactional
	public void handle(HandleTokenDeductionFailedCommand command) {
		if (processedMembershipEventPort.existsByEventUuid(command.eventUuid())) {
			log.warn(
					"HandleTokenDeductionFailedService : handle : 중복 TokenDeductionFailed 이벤트 무시 - eventUuid={}",
					command.eventUuid()
			);
			return;
		}
		MembershipPayment payment = loadPaymentPort.findByUuid(command.paymentUuid())
				.orElseThrow(() -> new InvalidPaymentStateException("결제 정보를 찾을 수 없습니다."));
		if (payment.paymentStatus() == PaymentStatus.FAILED) {
			markProcessed(command);
			return;
		}
		if (payment.paymentStatus() != PaymentStatus.READY) {
			log.error(
					"HandleTokenDeductionFailedService : handle : READY가 아닌 결제에 토큰 차감 실패가 수신되었다 - paymentUuid={}, status={}",
					payment.paymentUuid(),
					payment.paymentStatus()
			);
			markProcessed(command);
			return;
		}
		if (loadSubscriptionPort.findByUuid(payment.subscriptionUuid()).isPresent()) {
			log.error(
					"HandleTokenDeductionFailedService : handle : 토큰 차감 실패인데 구독이 이미 존재한다 - paymentUuid={}, subscriptionUuid={}",
					payment.paymentUuid(),
					payment.subscriptionUuid()
			);
		}
		Instant failedAt = command.failedAt() == null ? Instant.now() : command.failedAt();
		savePaymentPort.save(payment.fail());
		loadMembershipSagaPort.findByPaymentUuid(payment.paymentUuid())
				.ifPresent(saga -> saveMembershipSagaPort.save(saga.failed(failedAt)));
		markProcessed(command);
		log.info(
				"HandleTokenDeductionFailedService : handle : 토큰 차감 실패로 결제를 FAILED 처리하고 구독은 생성하지 않는다 - paymentUuid={}",
				payment.paymentUuid()
		);
	}

	private void markProcessed(HandleTokenDeductionFailedCommand command) {
		Instant processedAt = command.failedAt() == null ? Instant.now() : command.failedAt();
		processedMembershipEventPort.saveIdempotent(ProcessedMembershipEvent.recorded(
				command.eventUuid(),
				command.memberUuid(),
				MembershipEventTypes.TOKEN_DEDUCTION_FAILED,
				processedAt
		));
	}
}
