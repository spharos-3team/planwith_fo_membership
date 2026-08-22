package com.planwith.planwith_fo_membership.application.port.out;

public interface MembershipEventOutboxPort {

	void save(MembershipOutboxMessage message);
}
