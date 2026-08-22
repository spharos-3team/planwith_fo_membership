package com.planwith.planwith_fo_membership.adapter.in.kafka;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_membership.adapter.in.kafka.dto.PaymentFailedInboundEvent;
import com.planwith.planwith_fo_membership.application.command.HandlePaymentFailedCommand;
import com.planwith.planwith_fo_membership.application.port.in.command.HandlePaymentFailedUseCase;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.PaymentUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;

@Component
@ConditionalOnProperty(name = "membership.kafka.consumer-enabled", havingValue = "true")
public class PaymentFailedEventConsumer {

	private static final Logger log = LoggerFactory.getLogger(PaymentFailedEventConsumer.class);

	private final ObjectMapper objectMapper;
	private final HandlePaymentFailedUseCase handlePaymentFailedUseCase;

	public PaymentFailedEventConsumer(
			ObjectMapper objectMapper,
			HandlePaymentFailedUseCase handlePaymentFailedUseCase
	) {
		this.objectMapper = objectMapper;
		this.handlePaymentFailedUseCase = handlePaymentFailedUseCase;
	}

	@KafkaListener(topics = "${membership.kafka.topics.payment-failed}")
	public void consume(String payload) {
		try {
			PaymentFailedInboundEvent event = objectMapper.readValue(payload, PaymentFailedInboundEvent.class);
			if (event.eventUuid() == null || event.memberUuid() == null) {
				throw new IllegalArgumentException("PaymentFailed payload is invalid.");
			}
			log.info("PaymentFailedEventConsumer : consume : PaymentFailed 이벤트 수신 - eventUuid={}",
					event.eventUuid());
			handlePaymentFailedUseCase.handle(new HandlePaymentFailedCommand(
					UUID.fromString(event.eventUuid()),
					MemberUuid.from(event.memberUuid()),
					event.paymentUuid() == null ? null : PaymentUuid.from(event.paymentUuid()),
					event.subscriptionUuid() == null ? null : SubscriptionUuid.from(event.subscriptionUuid()),
					event.failedAt()
			));
		} catch (JsonProcessingException | IllegalArgumentException exception) {
			log.warn("PaymentFailedEventConsumer : consume : PaymentFailed 파싱 실패");
		}
	}
}
