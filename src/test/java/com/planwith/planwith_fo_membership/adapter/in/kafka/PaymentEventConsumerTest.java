package com.planwith.planwith_fo_membership.adapter.in.kafka;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_membership.application.port.in.command.HandlePaymentCompletedUseCase;
import com.planwith.planwith_fo_membership.application.port.in.command.HandlePaymentFailedUseCase;
import com.planwith.planwith_fo_membership.application.port.in.command.HandlePaymentRefundedUseCase;
import com.planwith.planwith_fo_membership.config.MembershipKafkaProperties;

class PaymentEventConsumerTest {

	private HandlePaymentCompletedUseCase completedUseCase;
	private HandlePaymentFailedUseCase failedUseCase;
	private HandlePaymentRefundedUseCase refundedUseCase;
	private PaymentCompletedEventConsumer completedConsumer;
	private PaymentFailedEventConsumer failedConsumer;
	private PaymentRefundedEventConsumer refundedConsumer;

	@BeforeEach
	void setUp() {
		completedUseCase = Mockito.mock(HandlePaymentCompletedUseCase.class);
		failedUseCase = Mockito.mock(HandlePaymentFailedUseCase.class);
		refundedUseCase = Mockito.mock(HandlePaymentRefundedUseCase.class);
		ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
		MembershipKafkaProperties kafkaProperties = new MembershipKafkaProperties();
		completedConsumer = new PaymentCompletedEventConsumer(objectMapper, completedUseCase, kafkaProperties);
		failedConsumer = new PaymentFailedEventConsumer(objectMapper, failedUseCase);
		refundedConsumer = new PaymentRefundedEventConsumer(objectMapper, refundedUseCase);
	}

	@Test
	void completedConsumerHandlesValidPayload() {
		completedConsumer.consume("""
				{
				  "eventUuid": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
				  "memberUuid": "11111111-1111-1111-1111-111111111111",
				  "creatorUuid": "22222222-2222-2222-2222-222222222222",
				  "paymentUuid": "cccccccc-cccc-cccc-cccc-cccccccccccc",
				  "subscriptionUuid": "55555555-5555-5555-5555-555555555555",
				  "completedAt": "2026-08-22T00:10:00Z"
				}
				""");

		verify(completedUseCase).handle(any());
	}

	@Test
	void failedAndRefundedConsumersHandleValidPayload() {
		failedConsumer.consume("""
				{
				  "eventUuid": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
				  "memberUuid": "11111111-1111-1111-1111-111111111111",
				  "paymentUuid": "cccccccc-cccc-cccc-cccc-cccccccccccc",
				  "subscriptionUuid": "55555555-5555-5555-5555-555555555555",
				  "failedAt": "2026-08-22T00:10:00Z"
				}
				""");
		refundedConsumer.consume("""
				{
				  "eventUuid": "cccccccc-cccc-cccc-cccc-cccccccccccc",
				  "memberUuid": "11111111-1111-1111-1111-111111111111",
				  "paymentUuid": "cccccccc-cccc-cccc-cccc-cccccccccccc",
				  "subscriptionUuid": "55555555-5555-5555-5555-555555555555",
				  "refundedAt": "2026-08-22T00:10:00Z"
				}
				""");

		verify(failedUseCase).handle(any());
		verify(refundedUseCase).handle(any());
	}

	@Test
	void consumersIgnorePayloadWithoutPaymentUuid() {
		completedConsumer.consume("""
				{
				  "eventUuid": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
				  "memberUuid": "11111111-1111-1111-1111-111111111111"
				}
				""");
		failedConsumer.consume("{}");
		refundedConsumer.consume("{invalid");

		verify(completedUseCase, never()).handle(any());
		verifyNoInteractions(failedUseCase);
		verifyNoInteractions(refundedUseCase);
	}
}
