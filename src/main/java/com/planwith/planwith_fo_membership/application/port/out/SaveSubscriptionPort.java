package com.planwith.planwith_fo_membership.application.port.out;

import com.planwith.planwith_fo_membership.domain.model.MembershipSubscription;

public interface SaveSubscriptionPort {

	void save(MembershipSubscription subscription);
}
