package com.planwith.planwith_fo_membership.adapter.in.kafka;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_membership.application.port.in.command.HandleTokenDeductionFailedUseCase;
import com.planwith.planwith_fo_membership.application.port.in.command.HandleTokenDeductionSucceededUseCase;
import com.planwith.planwith_fo_membership.config.MembershipKafkaProperties;

class TokenDeductionEventConsumerTest {

	private HandleTokenDeductionSucceededUseCase succeededUseCase;
	private HandleTokenDeductionFailedUseCase failedUseCase;
	private TokenDeductionSucceededEventConsumer succeededConsumer;
	private TokenDeductionFailedEventConsumer failedConsumer;

	@BeforeEach
	void setUp() {
		succeededUseCase = Mockito.mock(HandleTokenDeductionSucceededUseCase.class);
		failedUseCase = Mockito.mock(HandleTokenDeductionFailedUseCase.class);
		ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
		MembershipKafkaProperties kafkaProperties = new MembershipKafkaProperties();
		succeededConsumer = new TokenDeductionSucceededEventConsumer(objectMapper, succeededUseCase, kafkaProperties);
		failedConsumer = new TokenDeductionFailedEventConsumer(objectMapper, failedUseCase, kafkaProperties);
	}

	@Test
	void succeededConsumerHandlesValidPayload() {
		succeededConsumer.consume("""
				{
				  "eventUuid": "dddddddd-dddd-dddd-dddd-dddddddddddd",
				  "memberUuid": "11111111-1111-1111-1111-111111111111",
				  "creatorUuid": "22222222-2222-2222-2222-222222222222",
				  "paymentUuid": "cccccccc-cccc-cccc-cccc-cccccccccccc",
				  "subscriptionUuid": "55555555-5555-5555-5555-555555555555",
				  "succeededAt": "2026-08-22T00:10:00Z"
				}
				""");

		verify(succeededUseCase).handle(any());
	}

	@Test
	void failedConsumerHandlesValidPayload() {
		failedConsumer.consume("""
				{
				  "eventUuid": "eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee",
				  "memberUuid": "11111111-1111-1111-1111-111111111111",
				  "paymentUuid": "cccccccc-cccc-cccc-cccc-cccccccccccc",
				  "subscriptionUuid": "55555555-5555-5555-5555-555555555555",
				  "failedAt": "2026-08-22T00:10:00Z"
				}
				""");

		verify(failedUseCase).handle(any());
	}

	@Test
	void succeededConsumerIgnoresInvalidPayload() {
		succeededConsumer.consume("{invalid");

		verify(succeededUseCase, never()).handle(any());
	}

	@Test
	void failedConsumerIgnoresInvalidPayload() {
		failedConsumer.consume("{}");

		verifyNoInteractions(failedUseCase);
	}
}
