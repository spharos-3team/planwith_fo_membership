package com.planwith.planwith_fo_membership.adapter.in.kafka;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_membership.adapter.in.kafka.dto.PaymentRefundedInboundEvent;
import com.planwith.planwith_fo_membership.application.command.HandlePaymentRefundedCommand;
import com.planwith.planwith_fo_membership.application.port.in.command.HandlePaymentRefundedUseCase;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.PaymentUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;

@Component
@ConditionalOnProperty(name = "membership.kafka.consumer-enabled", havingValue = "true")
public class PaymentRefundedEventConsumer {

	private static final Logger log = LoggerFactory.getLogger(PaymentRefundedEventConsumer.class);

	private final ObjectMapper objectMapper;
	private final HandlePaymentRefundedUseCase handlePaymentRefundedUseCase;

	public PaymentRefundedEventConsumer(
			ObjectMapper objectMapper,
			HandlePaymentRefundedUseCase handlePaymentRefundedUseCase
	) {
		this.objectMapper = objectMapper;
		this.handlePaymentRefundedUseCase = handlePaymentRefundedUseCase;
	}

	@KafkaListener(topics = "${membership.kafka.topics.payment-refunded}")
	public void consume(String payload) {
		try {
			PaymentRefundedInboundEvent event = objectMapper.readValue(payload, PaymentRefundedInboundEvent.class);
			if (event.eventUuid() == null || event.memberUuid() == null) {
				throw new IllegalArgumentException("PaymentRefunded payload is invalid.");
			}
			log.info("PaymentRefundedEventConsumer : consume : PaymentRefunded 이벤트 수신 - eventUuid={}",
					event.eventUuid());
			handlePaymentRefundedUseCase.handle(new HandlePaymentRefundedCommand(
					UUID.fromString(event.eventUuid()),
					MemberUuid.from(event.memberUuid()),
					event.paymentUuid() == null ? null : PaymentUuid.from(event.paymentUuid()),
					event.subscriptionUuid() == null ? null : SubscriptionUuid.from(event.subscriptionUuid()),
					event.refundedAt()
			));
		} catch (JsonProcessingException | IllegalArgumentException exception) {
			log.warn("PaymentRefundedEventConsumer : consume : PaymentRefunded 파싱 실패");
		}
	}
}
