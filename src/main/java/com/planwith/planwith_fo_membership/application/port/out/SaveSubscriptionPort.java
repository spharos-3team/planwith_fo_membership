package com.planwith.planwith_fo_membership.application.port.out;

import com.planwith.planwith_fo_membership.domain.model.Subscription;

public interface SaveSubscriptionPort {

	void save(Subscription subscription);
}
