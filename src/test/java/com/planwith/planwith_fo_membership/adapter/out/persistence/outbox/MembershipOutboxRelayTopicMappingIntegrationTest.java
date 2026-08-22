package com.planwith.planwith_fo_membership.adapter.out.persistence.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.planwith.planwith_fo_membership.application.port.out.MembershipEventPublisher;
import com.planwith.planwith_fo_membership.config.MembershipKafkaProperties;
import com.planwith.planwith_fo_membership.config.MembershipOutboxProperties;
import com.planwith.planwith_fo_membership.domain.event.MembershipEventTypes;

@SpringBootTest
@ActiveProfiles("test")
class MembershipOutboxRelayTopicMappingIntegrationTest {

	@Autowired
	private SpringDataMembershipOutboxRepository repository;

	@Autowired
	private MembershipOutboxProperties outboxProperties;

	@Autowired
	private MembershipKafkaProperties kafkaProperties;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@ParameterizedTest
	@MethodSource("outputEvents")
	void relayPublishesEachOutputEventToConfiguredTopic(String eventType, String expectedTopic) {
		UUID eventUuid = UUID.randomUUID();
		UUID aggregateUuid = UUID.randomUUID();
		Instant now = Instant.parse("2026-08-22T02:00:00Z");
		MembershipEventPublisher publisher = mock(MembershipEventPublisher.class);

		TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
		transactionTemplate.executeWithoutResult(status -> repository.save(new MembershipOutboxJpaEntity(
				eventUuid,
				MembershipEventTypes.AGGREGATE_SUBSCRIPTION,
				aggregateUuid,
				eventType,
				"{\"eventType\":\"" + eventType + "\"}",
				now
		)));

		when(publisher.publish(eq(expectedTopic), eq(aggregateUuid.toString()), anyString()))
				.thenReturn(CompletableFuture.completedFuture(null));

		MembershipOutboxRelay relay = new MembershipOutboxRelay(
				repository,
				publisher,
				outboxProperties,
				kafkaProperties,
				fixedClockProvider(now)
		);
		relay.relayUnpublishedEvents();

		verify(publisher).publish(eq(expectedTopic), eq(aggregateUuid.toString()), anyString());
		assertThat(repository.findByEventUuid(eventUuid).orElseThrow().publishedAt()).isEqualTo(now);
	}

	@SuppressWarnings("unchecked")
	private static ObjectProvider<Clock> fixedClockProvider(Instant now) {
		ObjectProvider<Clock> clockProvider = mock(ObjectProvider.class);
		when(clockProvider.getIfAvailable()).thenReturn(Clock.fixed(now, ZoneOffset.UTC));
		return clockProvider;
	}

	static Stream<Arguments> outputEvents() {
		return Stream.of(
				Arguments.of(MembershipEventTypes.TOKEN_DEDUCTION_REQUESTED, "planwith.token.deduction-requested"),
				Arguments.of(MembershipEventTypes.MEMBERSHIP_SUBSCRIBED, "planwith.membership.subscribed"),
				Arguments.of(MembershipEventTypes.MEMBERSHIP_CANCELED, "planwith.membership.canceled"),
				Arguments.of(MembershipEventTypes.MEMBERSHIP_EXPIRED, "planwith.membership.expired"),
				Arguments.of(MembershipEventTypes.SETTLEMENT_REQUESTED, "planwith.membership.settlement-requested"),
				Arguments.of(MembershipEventTypes.SETTLEMENT_COMPLETED, "planwith.membership.settlement-completed")
		);
	}
}
