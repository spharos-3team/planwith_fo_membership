package com.planwith.planwith_fo_membership.adapter.in.kafka;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_membership.adapter.in.kafka.dto.PaymentCompletedInboundEvent;
import com.planwith.planwith_fo_membership.application.command.HandlePaymentCompletedCommand;
import com.planwith.planwith_fo_membership.application.port.in.command.HandlePaymentCompletedUseCase;
import com.planwith.planwith_fo_membership.config.MembershipKafkaProperties;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.PaymentUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;

@Component
@ConditionalOnProperty(name = "membership.kafka.consumer-enabled", havingValue = "true")
public class PaymentCompletedEventConsumer {

	private static final Logger log = LoggerFactory.getLogger(PaymentCompletedEventConsumer.class);

	private final ObjectMapper objectMapper;
	private final HandlePaymentCompletedUseCase handlePaymentCompletedUseCase;
	private final MembershipKafkaProperties kafkaProperties;

	public PaymentCompletedEventConsumer(
			ObjectMapper objectMapper,
			HandlePaymentCompletedUseCase handlePaymentCompletedUseCase,
			MembershipKafkaProperties kafkaProperties
	) {
		this.objectMapper = objectMapper;
		this.handlePaymentCompletedUseCase = handlePaymentCompletedUseCase;
		this.kafkaProperties = kafkaProperties;
	}

	@KafkaListener(topics = "${membership.kafka.topics.payment-completed}")
	public void consume(String payload) {
		try {
			PaymentCompletedInboundEvent event = objectMapper.readValue(payload, PaymentCompletedInboundEvent.class);
			if (event.eventUuid() == null || event.memberUuid() == null) {
				throw new IllegalArgumentException("PaymentCompleted payload is invalid.");
			}
			log.info("PaymentCompletedEventConsumer : consume : PaymentCompleted 이벤트 수신 - eventUuid={}",
					event.eventUuid());
			handlePaymentCompletedUseCase.handle(new HandlePaymentCompletedCommand(
					UUID.fromString(event.eventUuid()),
					MemberUuid.from(event.memberUuid()),
					event.creatorUuid() == null ? null : CreatorUuid.from(event.creatorUuid()),
					event.paymentUuid() == null ? null : PaymentUuid.from(event.paymentUuid()),
					event.subscriptionUuid() == null ? null : SubscriptionUuid.from(event.subscriptionUuid()),
					event.completedAt()
			));
		} catch (JsonProcessingException | IllegalArgumentException exception) {
			log.warn("PaymentCompletedEventConsumer : consume : PaymentCompleted 파싱 실패");
		} catch (RuntimeException exception) {
			log.warn("PaymentCompletedEventConsumer : consume : PaymentCompleted 처리 실패 - topic={}",
					kafkaProperties.getTopics().getPaymentCompleted());
			throw exception;
		}
	}
}
