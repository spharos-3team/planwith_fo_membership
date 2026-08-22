package com.planwith.planwith_fo_membership.adapter.out.persistence.processed;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.planwith.planwith_fo_membership.application.port.out.ProcessedMembershipEventPort;
import com.planwith.planwith_fo_membership.domain.model.ProcessedMembershipEvent;

public class InMemoryProcessedMembershipEventPort implements ProcessedMembershipEventPort {

	private final Set<UUID> processed = new HashSet<>();

	@Override
	public boolean existsByEventUuid(UUID eventUuid) {
		return processed.contains(eventUuid);
	}

	@Override
	public void save(ProcessedMembershipEvent event) {
		processed.add(event.eventUuid());
	}

	@Override
	public boolean saveIdempotent(ProcessedMembershipEvent event) {
		return processed.add(event.eventUuid());
	}
}
