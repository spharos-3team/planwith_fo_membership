package com.planwith.planwith_fo_membership.application.service;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_membership.application.command.HandlePaymentFailedCommand;
import com.planwith.planwith_fo_membership.application.port.in.command.HandlePaymentFailedUseCase;
import com.planwith.planwith_fo_membership.application.port.out.ProcessedMembershipEventPort;
import com.planwith.planwith_fo_membership.domain.event.MembershipEventTypes;
import com.planwith.planwith_fo_membership.domain.model.ProcessedMembershipEvent;

@Service
public class HandlePaymentFailedService implements HandlePaymentFailedUseCase {

	private static final Logger log = LoggerFactory.getLogger(HandlePaymentFailedService.class);

	private final ProcessedMembershipEventPort processedMembershipEventPort;

	public HandlePaymentFailedService(ProcessedMembershipEventPort processedMembershipEventPort) {
		this.processedMembershipEventPort = processedMembershipEventPort;
	}

	@Override
	@Transactional
	public void handle(HandlePaymentFailedCommand command) {
		if (processedMembershipEventPort.existsByEventUuid(command.eventUuid())) {
			log.warn("HandlePaymentFailedService : handle : 중복 PaymentFailed 이벤트 무시 - eventUuid={}",
					command.eventUuid());
			return;
		}
		log.info(
				"HandlePaymentFailedService : handle : PaymentFailed 수신, 보상 처리는 후속 Saga 이슈에서 구현한다 - eventUuid={}, memberUuid={}",
				command.eventUuid(),
				command.memberUuid()
		);
		Instant processedAt = command.failedAt() == null ? Instant.now() : command.failedAt();
		processedMembershipEventPort.saveIdempotent(ProcessedMembershipEvent.recorded(
				command.eventUuid(),
				command.memberUuid(),
				MembershipEventTypes.PAYMENT_FAILED,
				processedAt
		));
	}
}
