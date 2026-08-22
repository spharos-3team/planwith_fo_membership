package com.planwith.planwith_fo_membership.adapter.out.persistence.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
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
class MembershipOutboxRelayIntegrationTest {

	@Autowired
	private SpringDataMembershipOutboxRepository repository;

	@Autowired
	private MembershipOutboxProperties outboxProperties;

	@Autowired
	private MembershipKafkaProperties kafkaProperties;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Test
	void relayPublishesToKafkaAndMarksPublishedAt() {
		UUID eventUuid = UUID.fromString("d1111111-1111-1111-1111-111111111111");
		UUID aggregateUuid = UUID.fromString("d2222222-2222-2222-2222-222222222222");
		Instant now = Instant.parse("2026-08-22T12:00:00Z");
		MembershipEventPublisher publisher = mock(MembershipEventPublisher.class);
		saveOutbox(eventUuid, aggregateUuid, MembershipEventTypes.MEMBERSHIP_SUBSCRIBED, now);

		when(publisher.publish(
				eq(kafkaProperties.getTopics().getMembershipSubscribed()),
				eq(aggregateUuid.toString()),
				anyString()
		)).thenReturn(CompletableFuture.completedFuture(null));

		relay(publisher, now).relayUnpublishedEvents();

		MembershipOutboxJpaEntity published = repository.findByEventUuid(eventUuid).orElseThrow();
		assertThat(published.publishedAt()).isEqualTo(now);
		assertThat(published.retryCount()).isZero();
	}

	@Test
	void relayKeepsOutboxUnpublishedWhenKafkaFailsThenPublishesOnRetry() {
		UUID eventUuid = UUID.fromString("d3333333-3333-3333-3333-333333333333");
		UUID aggregateUuid = UUID.fromString("d4444444-4444-4444-4444-444444444444");
		Instant now = Instant.parse("2026-08-22T12:00:00Z");
		MembershipEventPublisher publisher = mock(MembershipEventPublisher.class);
		saveOutbox(eventUuid, aggregateUuid, MembershipEventTypes.SETTLEMENT_COMPLETED, now);

		when(publisher.publish(
				eq(kafkaProperties.getTopics().getSettlementCompleted()),
				eq(aggregateUuid.toString()),
				anyString()
		)).thenReturn(CompletableFuture.failedFuture(new RuntimeException("kafka down")))
				.thenReturn(CompletableFuture.completedFuture(null));

		relay(publisher, now).relayUnpublishedEvents();

		MembershipOutboxJpaEntity unpublished = repository.findByEventUuid(eventUuid).orElseThrow();
		assertThat(unpublished.publishedAt()).isNull();
		assertThat(unpublished.retryCount()).isEqualTo(1);
		assertThat(unpublished.nextRetryAt()).isAfter(now);

		Instant retryAt = unpublished.nextRetryAt();
		relay(publisher, retryAt).relayUnpublishedEvents();

		MembershipOutboxJpaEntity published = repository.findByEventUuid(eventUuid).orElseThrow();
		assertThat(published.publishedAt()).isEqualTo(retryAt);
		assertThat(published.retryCount()).isEqualTo(1);
	}

	@Test
	void relayDoesNotPublishUnknownEventType() {
		UUID eventUuid = UUID.fromString("d5555555-5555-5555-5555-555555555555");
		UUID aggregateUuid = UUID.fromString("d6666666-6666-6666-6666-666666666666");
		Instant now = Instant.parse("2026-08-22T12:00:00Z");
		MembershipEventPublisher publisher = mock(MembershipEventPublisher.class);
		saveOutbox(eventUuid, aggregateUuid, "UnknownEvent", now);

		relay(publisher, now).relayUnpublishedEvents();

		MembershipOutboxJpaEntity unpublished = repository.findByEventUuid(eventUuid).orElseThrow();
		assertThat(unpublished.publishedAt()).isNull();
		assertThat(unpublished.retryCount()).isEqualTo(1);
	}

	private void saveOutbox(UUID eventUuid, UUID aggregateUuid, String eventType, Instant occurredAt) {
		TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
		transactionTemplate.executeWithoutResult(status -> repository.save(new MembershipOutboxJpaEntity(
				eventUuid,
				MembershipEventTypes.AGGREGATE_SUBSCRIPTION,
				aggregateUuid,
				eventType,
				"{\"eventType\":\"" + eventType + "\"}",
				occurredAt
		)));
	}

	private MembershipOutboxRelay relay(MembershipEventPublisher publisher, Instant now) {
		return new MembershipOutboxRelay(
				repository,
				publisher,
				outboxProperties,
				kafkaProperties,
				fixedClockProvider(now)
		);
	}

	@SuppressWarnings("unchecked")
	private static ObjectProvider<Clock> fixedClockProvider(Instant now) {
		ObjectProvider<Clock> clockProvider = mock(ObjectProvider.class);
		when(clockProvider.getIfAvailable()).thenReturn(Clock.fixed(now, ZoneOffset.UTC));
		return clockProvider;
	}
}
