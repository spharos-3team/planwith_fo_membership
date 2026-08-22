package com.planwith.planwith_fo_membership.adapter.out.persistence.processed;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.planwith.planwith_fo_membership.application.port.out.ProcessedMembershipEventPort;
import com.planwith.planwith_fo_membership.domain.model.ProcessedMembershipEvent;

public class InMemoryProcessedMembershipEventPort implements ProcessedMembershipEventPort {

	private final List<ProcessedMembershipEvent> events = new ArrayList<>();

	@Override
	public boolean existsByEventUuid(UUID eventUuid) {
		return events.stream().anyMatch(event -> event.eventUuid().equals(eventUuid));
	}

	@Override
	public boolean existsByPaymentUuidAndEventType(UUID paymentUuid, String eventType) {
		return events.stream().anyMatch(event ->
				eventType.equals(event.eventType()) && Objects.equals(paymentUuid, event.paymentUuid()));
	}

	@Override
	public boolean existsBySettlementUuidAndEventType(UUID settlementUuid, String eventType) {
		return events.stream().anyMatch(event ->
				eventType.equals(event.eventType()) && Objects.equals(settlementUuid, event.settlementUuid()));
	}

	@Override
	public void save(ProcessedMembershipEvent event) {
		events.add(event);
	}

	@Override
	public boolean saveIdempotent(ProcessedMembershipEvent event) {
		if (alreadyProcessed(event.eventUuid(), event.paymentUuid(), event.settlementUuid(), event.eventType())) {
			return false;
		}
		events.add(event);
		return true;
	}
}
