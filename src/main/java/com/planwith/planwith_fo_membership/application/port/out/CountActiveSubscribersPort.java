package com.planwith.planwith_fo_membership.application.port.out;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface CountActiveSubscribersPort {

	Map<UUID, Long> countActiveByCreatorUuids(Collection<UUID> creatorUuids);
}
