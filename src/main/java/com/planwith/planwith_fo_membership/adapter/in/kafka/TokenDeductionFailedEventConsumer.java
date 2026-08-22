package com.planwith.planwith_fo_membership.adapter.in.kafka;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_membership.adapter.in.kafka.dto.TokenDeductionFailedInboundEvent;
import com.planwith.planwith_fo_membership.application.command.HandleTokenDeductionFailedCommand;
import com.planwith.planwith_fo_membership.application.port.in.command.HandleTokenDeductionFailedUseCase;
import com.planwith.planwith_fo_membership.config.MembershipKafkaProperties;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.PaymentUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;

@Component
@ConditionalOnProperty(name = "membership.kafka.consumer-enabled", havingValue = "true")
public class TokenDeductionFailedEventConsumer {

	private static final Logger log = LoggerFactory.getLogger(TokenDeductionFailedEventConsumer.class);

	private final ObjectMapper objectMapper;
	private final HandleTokenDeductionFailedUseCase handleTokenDeductionFailedUseCase;
	private final MembershipKafkaProperties kafkaProperties;

	public TokenDeductionFailedEventConsumer(
			ObjectMapper objectMapper,
			HandleTokenDeductionFailedUseCase handleTokenDeductionFailedUseCase,
			MembershipKafkaProperties kafkaProperties
	) {
		this.objectMapper = objectMapper;
		this.handleTokenDeductionFailedUseCase = handleTokenDeductionFailedUseCase;
		this.kafkaProperties = kafkaProperties;
	}

	@KafkaListener(topics = "${membership.kafka.topics.token-deduction-failed}")
	public void consume(String payload) {
		try {
			TokenDeductionFailedInboundEvent event = objectMapper.readValue(
					payload,
					TokenDeductionFailedInboundEvent.class
			);
			if (event.eventUuid() == null || event.memberUuid() == null || event.paymentUuid() == null) {
				throw new IllegalArgumentException("TokenDeductionFailed payload is invalid.");
			}
			log.info(
					"TokenDeductionFailedEventConsumer : consume : TokenDeductionFailed 이벤트 수신 - eventUuid={}, paymentUuid={}",
					event.eventUuid(),
					event.paymentUuid()
			);
			handleTokenDeductionFailedUseCase.handle(new HandleTokenDeductionFailedCommand(
					UUID.fromString(event.eventUuid()),
					MemberUuid.from(event.memberUuid()),
					PaymentUuid.from(event.paymentUuid()),
					event.subscriptionUuid() == null ? null : SubscriptionUuid.from(event.subscriptionUuid()),
					event.failedAt()
			));
		} catch (JsonProcessingException | IllegalArgumentException exception) {
			log.warn("TokenDeductionFailedEventConsumer : consume : TokenDeductionFailed 파싱 실패");
		} catch (RuntimeException exception) {
			log.warn(
					"TokenDeductionFailedEventConsumer : consume : TokenDeductionFailed 처리 실패 - topic={}",
					kafkaProperties.getTopics().getTokenDeductionFailed()
			);
			throw exception;
		}
	}
}
