package com.planwith.planwith_fo_membership.adapter.out.persistence.processed;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataProcessedMembershipEventRepository extends JpaRepository<ProcessedMembershipEventJpaEntity, Long> {

	boolean existsByEventUuid(UUID eventUuid);

	boolean existsByPaymentUuidAndEventType(UUID paymentUuid, String eventType);

	boolean existsBySettlementUuidAndEventType(UUID settlementUuid, String eventType);
}
