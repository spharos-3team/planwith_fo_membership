package com.planwith.planwith_fo_membership.adapter.in.kafka;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_membership.adapter.in.kafka.dto.TokenDeductionSucceededInboundEvent;
import com.planwith.planwith_fo_membership.application.command.HandleTokenDeductionSucceededCommand;
import com.planwith.planwith_fo_membership.application.port.in.command.HandleTokenDeductionSucceededUseCase;
import com.planwith.planwith_fo_membership.config.MembershipKafkaProperties;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.PaymentUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;

@Component
@ConditionalOnProperty(name = "membership.kafka.consumer-enabled", havingValue = "true")
public class TokenDeductionSucceededEventConsumer {

	private static final Logger log = LoggerFactory.getLogger(TokenDeductionSucceededEventConsumer.class);

	private final ObjectMapper objectMapper;
	private final HandleTokenDeductionSucceededUseCase handleTokenDeductionSucceededUseCase;
	private final MembershipKafkaProperties kafkaProperties;

	public TokenDeductionSucceededEventConsumer(
			ObjectMapper objectMapper,
			HandleTokenDeductionSucceededUseCase handleTokenDeductionSucceededUseCase,
			MembershipKafkaProperties kafkaProperties
	) {
		this.objectMapper = objectMapper;
		this.handleTokenDeductionSucceededUseCase = handleTokenDeductionSucceededUseCase;
		this.kafkaProperties = kafkaProperties;
	}

	@KafkaListener(topics = "${membership.kafka.topics.token-deduction-succeeded}")
	public void consume(String payload) {
		try {
			TokenDeductionSucceededInboundEvent event = objectMapper.readValue(
					payload,
					TokenDeductionSucceededInboundEvent.class
			);
			if (event.eventUuid() == null || event.memberUuid() == null || event.paymentUuid() == null) {
				throw new IllegalArgumentException("TokenDeductionSucceeded payload is invalid.");
			}
			log.info(
					"TokenDeductionSucceededEventConsumer : consume : TokenDeductionSucceeded 이벤트 수신 - eventUuid={}, paymentUuid={}",
					event.eventUuid(),
					event.paymentUuid()
			);
			handleTokenDeductionSucceededUseCase.handle(new HandleTokenDeductionSucceededCommand(
					UUID.fromString(event.eventUuid()),
					MemberUuid.from(event.memberUuid()),
					event.creatorUuid() == null ? null : CreatorUuid.from(event.creatorUuid()),
					PaymentUuid.from(event.paymentUuid()),
					event.subscriptionUuid() == null ? null : SubscriptionUuid.from(event.subscriptionUuid()),
					event.succeededAt()
			));
		} catch (JsonProcessingException | IllegalArgumentException exception) {
			log.warn("TokenDeductionSucceededEventConsumer : consume : TokenDeductionSucceeded 파싱 실패");
		} catch (RuntimeException exception) {
			log.warn(
					"TokenDeductionSucceededEventConsumer : consume : TokenDeductionSucceeded 처리 실패 - topic={}",
					kafkaProperties.getTopics().getTokenDeductionSucceeded()
			);
			throw exception;
		}
	}
}
