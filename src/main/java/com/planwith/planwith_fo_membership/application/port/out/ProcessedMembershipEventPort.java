package com.planwith.planwith_fo_membership.application.port.out;

import java.util.UUID;

import com.planwith.planwith_fo_membership.domain.model.ProcessedMembershipEvent;

public interface ProcessedMembershipEventPort {

	boolean existsByEventUuid(UUID eventUuid);

	boolean existsByPaymentUuidAndEventType(UUID paymentUuid, String eventType);

	boolean existsBySettlementUuidAndEventType(UUID settlementUuid, String eventType);

	void save(ProcessedMembershipEvent event);

	boolean saveIdempotent(ProcessedMembershipEvent event);

	default boolean alreadyProcessed(
			UUID eventUuid,
			UUID paymentUuid,
			UUID settlementUuid,
			String eventType
	) {
		if (eventUuid != null && existsByEventUuid(eventUuid)) {
			return true;
		}
		if (paymentUuid != null && existsByPaymentUuidAndEventType(paymentUuid, eventType)) {
			return true;
		}
		if (settlementUuid != null && existsBySettlementUuidAndEventType(settlementUuid, eventType)) {
			return true;
		}
		return false;
	}
}
