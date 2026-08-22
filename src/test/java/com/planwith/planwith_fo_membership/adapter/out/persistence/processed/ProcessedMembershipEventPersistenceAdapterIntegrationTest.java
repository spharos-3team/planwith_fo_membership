package com.planwith.planwith_fo_membership.adapter.out.persistence.processed;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_membership.application.port.out.ProcessedMembershipEventPort;
import com.planwith.planwith_fo_membership.domain.event.MembershipEventTypes;
import com.planwith.planwith_fo_membership.domain.model.ProcessedMembershipEvent;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProcessedMembershipEventPersistenceAdapterIntegrationTest {

	private final MemberUuid memberUuid = MemberUuid.from("11111111-1111-1111-1111-111111111111");
	private final UUID eventUuid = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
	private final UUID paymentUuid = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
	private final UUID settlementUuid = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");

	@Autowired
	private ProcessedMembershipEventPort processedMembershipEventPort;

	@Test
	void recordsEventAndLooksUpByEventPaymentAndSettlement() {
		boolean saved = processedMembershipEventPort.saveIdempotent(ProcessedMembershipEvent.recorded(
				eventUuid,
				memberUuid,
				MembershipEventTypes.SETTLEMENT_COMPLETED,
				paymentUuid,
				settlementUuid,
				Instant.parse("2026-08-22T01:00:00Z")
		));

		assertThat(saved).isTrue();
		assertThat(processedMembershipEventPort.existsByEventUuid(eventUuid)).isTrue();
		assertThat(processedMembershipEventPort.existsByPaymentUuidAndEventType(
				paymentUuid,
				MembershipEventTypes.SETTLEMENT_COMPLETED
		)).isTrue();
		assertThat(processedMembershipEventPort.existsBySettlementUuidAndEventType(
				settlementUuid,
				MembershipEventTypes.SETTLEMENT_COMPLETED
		)).isTrue();
		assertThat(processedMembershipEventPort.alreadyProcessed(
				UUID.randomUUID(),
				paymentUuid,
				null,
				MembershipEventTypes.SETTLEMENT_COMPLETED
		)).isTrue();
		assertThat(processedMembershipEventPort.alreadyProcessed(
				UUID.randomUUID(),
				null,
				settlementUuid,
				MembershipEventTypes.SETTLEMENT_COMPLETED
		)).isTrue();
		assertThat(processedMembershipEventPort.saveIdempotent(ProcessedMembershipEvent.recorded(
				UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
				memberUuid,
				MembershipEventTypes.SETTLEMENT_COMPLETED,
				paymentUuid,
				settlementUuid,
				Instant.parse("2026-08-22T01:01:00Z")
		))).isFalse();
	}
}
