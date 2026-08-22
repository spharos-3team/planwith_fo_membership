package com.planwith.planwith_fo_membership.application.port.in.command;

import java.time.Instant;

public interface ExpireDueSubscriptionsUseCase {

	int expireDue(Instant now);
}
