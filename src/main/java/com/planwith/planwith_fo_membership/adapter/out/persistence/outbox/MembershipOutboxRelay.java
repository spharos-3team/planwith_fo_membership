package com.planwith.planwith_fo_membership.adapter.out.persistence.outbox;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_membership.application.port.out.MembershipEventPublisher;
import com.planwith.planwith_fo_membership.config.MembershipKafkaProperties;
import com.planwith.planwith_fo_membership.config.MembershipOutboxProperties;
import com.planwith.planwith_fo_membership.domain.event.MembershipEventTypes;

@Component
@ConditionalOnProperty(name = "membership.outbox.enabled", havingValue = "true")
public class MembershipOutboxRelay {

	private static final Logger log = LoggerFactory.getLogger(MembershipOutboxRelay.class);

	private final SpringDataMembershipOutboxRepository repository;
	private final MembershipEventPublisher publisher;
	private final MembershipOutboxProperties outboxProperties;
	private final MembershipKafkaProperties kafkaProperties;
	private final Clock clock;

	public MembershipOutboxRelay(
			SpringDataMembershipOutboxRepository repository,
			MembershipEventPublisher publisher,
			MembershipOutboxProperties outboxProperties,
			MembershipKafkaProperties kafkaProperties,
			ObjectProvider<Clock> clockProvider
	) {
		this.repository = repository;
		this.publisher = publisher;
		this.outboxProperties = outboxProperties;
		this.kafkaProperties = kafkaProperties;
		Clock provided = clockProvider.getIfAvailable();
		this.clock = provided == null ? Clock.systemUTC() : provided;
	}

	@Scheduled(
			fixedDelayString = "${membership.outbox.relay-interval:5s}",
			initialDelayString = "${membership.outbox.relay-initial-delay:5s}"
	)
	@Transactional
	public void relayUnpublishedEvents() {
		int batchSize = outboxProperties.getRelayBatchSize() > 0
				? outboxProperties.getRelayBatchSize()
				: 50;
		Instant now = clock.instant();
		List<MembershipOutboxJpaEntity> unpublished = repository.findDueUnpublished(now, PageRequest.of(0, batchSize));
		for (MembershipOutboxJpaEntity outbox : unpublished) {
			if (outbox.isDue(now)) {
				publish(outbox, now);
			}
		}
	}

	private void publish(MembershipOutboxJpaEntity outbox, Instant now) {
		try {
			publisher.publish(
							topicFor(outbox.eventType()),
							outbox.aggregateUuid().toString(),
							outbox.payload()
					)
					.get(sendTimeoutMillis(), TimeUnit.MILLISECONDS);
			outbox.markPublished(now);
			repository.save(outbox);
			log.info("MembershipOutboxRelay : publish : 멤버십 Outbox 발행 완료 - eventUuid={}, eventType={}",
					outbox.eventUuid(), outbox.eventType());
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			recordFailure(outbox, now);
			log.warn("MembershipOutboxRelay : publish : 멤버십 Outbox 발행 중단 - eventUuid={}, retryCount={}",
					outbox.eventUuid(), outbox.retryCount());
		} catch (Exception exception) {
			recordFailure(outbox, now);
			if (outboxProperties.retryLimitReached(outbox.retryCount())) {
				log.error(
						"MembershipOutboxRelay : publish : 멤버십 Outbox 최대 재시도 이후에도 미발행 유지 - eventUuid={}, retryCount={}",
						outbox.eventUuid(),
						outbox.retryCount()
				);
			} else {
				log.warn("MembershipOutboxRelay : publish : 멤버십 Outbox 발행 실패 - eventUuid={}, retryCount={}",
						outbox.eventUuid(), outbox.retryCount());
			}
		}
	}

	private void recordFailure(MembershipOutboxJpaEntity outbox, Instant now) {
		int nextRetryCount = outbox.retryCount() + 1;
		outbox.recordPublishFailure(outboxProperties.nextRetryAt(now, nextRetryCount));
		repository.save(outbox);
	}

	private String topicFor(String eventType) {
		if (MembershipEventTypes.TOKEN_DEDUCTION_REQUESTED.equals(eventType)) {
			return kafkaProperties.getTopics().getTokenDeductionRequested();
		}
		if (MembershipEventTypes.MEMBERSHIP_SUBSCRIBED.equals(eventType)) {
			return kafkaProperties.getTopics().getMembershipSubscribed();
		}
		if (MembershipEventTypes.MEMBERSHIP_CANCELED.equals(eventType)) {
			return kafkaProperties.getTopics().getMembershipCanceled();
		}
		if (MembershipEventTypes.MEMBERSHIP_EXPIRED.equals(eventType)) {
			return kafkaProperties.getTopics().getMembershipExpired();
		}
		if (MembershipEventTypes.SETTLEMENT_REQUESTED.equals(eventType)) {
			return kafkaProperties.getTopics().getSettlementRequested();
		}
		if (MembershipEventTypes.SETTLEMENT_COMPLETED.equals(eventType)) {
			return kafkaProperties.getTopics().getSettlementCompleted();
		}
		throw new IllegalArgumentException("알 수 없는 Outbox eventType 입니다: " + eventType);
	}

	private long sendTimeoutMillis() {
		Duration timeout = outboxProperties.getSendTimeout();
		if (timeout == null || timeout.isZero() || timeout.isNegative()) {
			return Duration.ofSeconds(10).toMillis();
		}
		return timeout.toMillis();
	}
}
