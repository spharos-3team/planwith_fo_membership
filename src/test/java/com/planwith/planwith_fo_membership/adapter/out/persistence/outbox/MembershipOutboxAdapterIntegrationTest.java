package com.planwith.planwith_fo_membership.adapter.out.persistence.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_membership.application.port.out.MembershipEventOutboxPort;
import com.planwith.planwith_fo_membership.application.port.out.MembershipOutboxMessage;
import com.planwith.planwith_fo_membership.domain.event.MembershipEventTypes;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MembershipOutboxAdapterIntegrationTest {

	@Autowired
	private MembershipEventOutboxPort membershipEventOutboxPort;

	@Autowired
	private SpringDataMembershipOutboxRepository repository;

	@Test
	void saveOutboxMessageWithinActiveTransaction() {
		Instant occurredAt = Instant.parse("2026-08-22T00:00:00Z");
		membershipEventOutboxPort.save(new MembershipOutboxMessage(
				"33333333-3333-3333-3333-333333333333",
				MembershipEventTypes.AGGREGATE_SUBSCRIPTION,
				"22222222-2222-2222-2222-222222222222",
				MembershipEventTypes.MEMBERSHIP_SUBSCRIBED,
				"{\"allowed\":true}",
				occurredAt
		));

		assertThat(repository.existsByEventUuid(
				UUID.fromString("33333333-3333-3333-3333-333333333333")
		)).isTrue();
		MembershipOutboxJpaEntity saved = repository.findByEventUuid(
				UUID.fromString("33333333-3333-3333-3333-333333333333")
		).orElseThrow();
		assertThat(saved.occurredAt()).isEqualTo(occurredAt);
		assertThat(saved.publishedAt()).isNull();
		assertThat(saved.retryCount()).isZero();
	}

	@Test
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	void saveRequiresActiveTransaction() {
		assertThatThrownBy(() -> membershipEventOutboxPort.save(new MembershipOutboxMessage(
				"44444444-4444-4444-4444-444444444444",
				MembershipEventTypes.AGGREGATE_SUBSCRIPTION,
				"22222222-2222-2222-2222-222222222222",
				MembershipEventTypes.MEMBERSHIP_SUBSCRIBED,
				"{}",
				Instant.now()
		))).isInstanceOf(IllegalTransactionStateException.class);
	}
}
