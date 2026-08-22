package com.planwith.planwith_fo_membership.application.service;

import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_membership.application.command.HandlePaymentRefundedCommand;
import com.planwith.planwith_fo_membership.application.port.in.command.HandlePaymentRefundedUseCase;
import com.planwith.planwith_fo_membership.application.port.out.ProcessedMembershipEventPort;
import com.planwith.planwith_fo_membership.domain.event.MembershipEventTypes;
import com.planwith.planwith_fo_membership.domain.model.ProcessedMembershipEvent;

@Service
public class HandlePaymentRefundedService implements HandlePaymentRefundedUseCase {

	private static final Logger log = LoggerFactory.getLogger(HandlePaymentRefundedService.class);

	private final ProcessedMembershipEventPort processedMembershipEventPort;

	public HandlePaymentRefundedService(ProcessedMembershipEventPort processedMembershipEventPort) {
		this.processedMembershipEventPort = processedMembershipEventPort;
	}

	@Override
	@Transactional
	public void handle(HandlePaymentRefundedCommand command) {
		UUID paymentUuid = command.paymentUuid() == null ? null : command.paymentUuid().value();
		if (processedMembershipEventPort.alreadyProcessed(
				command.eventUuid(),
				paymentUuid,
				null,
				MembershipEventTypes.PAYMENT_REFUNDED
		)) {
			log.warn(
					"HandlePaymentRefundedService : handle : 중복 PaymentRefunded 이벤트 무시 - eventUuid={}, paymentUuid={}",
					command.eventUuid(),
					command.paymentUuid()
			);
			return;
		}
		log.info(
				"HandlePaymentRefundedService : handle : PaymentRefunded 수신, 환불 보상은 후속 Saga에서 처리한다 - eventUuid={}, paymentUuid={}",
				command.eventUuid(),
				command.paymentUuid()
		);
		Instant processedAt = command.refundedAt() == null ? Instant.now() : command.refundedAt();
		processedMembershipEventPort.saveIdempotent(ProcessedMembershipEvent.recorded(
				command.eventUuid(),
				command.memberUuid(),
				MembershipEventTypes.PAYMENT_REFUNDED,
				paymentUuid,
				null,
				processedAt
		));
	}
}
