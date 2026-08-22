package com.planwith.planwith_fo_membership.application.port.out;

import java.util.UUID;

import com.planwith.planwith_fo_membership.domain.model.ProcessedMembershipEvent;

public interface ProcessedMembershipEventPort {

	boolean existsByEventUuid(UUID eventUuid);

	void save(ProcessedMembershipEvent event);

	boolean saveIdempotent(ProcessedMembershipEvent event);
}
