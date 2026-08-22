package com.planwith.planwith_fo_membership.application.service;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_membership.application.command.HandlePaymentCompletedCommand;
import com.planwith.planwith_fo_membership.application.port.in.command.HandlePaymentCompletedUseCase;
import com.planwith.planwith_fo_membership.application.port.out.ProcessedMembershipEventPort;
import com.planwith.planwith_fo_membership.domain.event.MembershipEventTypes;
import com.planwith.planwith_fo_membership.domain.model.ProcessedMembershipEvent;

@Service
public class HandlePaymentCompletedService implements HandlePaymentCompletedUseCase {

	private static final Logger log = LoggerFactory.getLogger(HandlePaymentCompletedService.class);

	private final ProcessedMembershipEventPort processedMembershipEventPort;

	public HandlePaymentCompletedService(ProcessedMembershipEventPort processedMembershipEventPort) {
		this.processedMembershipEventPort = processedMembershipEventPort;
	}

	@Override
	@Transactional
	public void handle(HandlePaymentCompletedCommand command) {
		if (processedMembershipEventPort.existsByEventUuid(command.eventUuid())) {
			log.warn("HandlePaymentCompletedService : handle : 중복 PaymentCompleted 이벤트 무시 - eventUuid={}",
					command.eventUuid());
			return;
		}
		log.info(
				"HandlePaymentCompletedService : handle : PaymentCompleted 수신, 구독 활성화는 후속 Saga 이슈에서 구현한다 - eventUuid={}, memberUuid={}",
				command.eventUuid(),
				command.memberUuid()
		);
		Instant processedAt = command.completedAt() == null ? Instant.now() : command.completedAt();
		processedMembershipEventPort.saveIdempotent(ProcessedMembershipEvent.recorded(
				command.eventUuid(),
				command.memberUuid(),
				MembershipEventTypes.PAYMENT_COMPLETED,
				processedAt
		));
	}
}
