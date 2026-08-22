package com.planwith.planwith_fo_membership.application.port.out;

import com.planwith.planwith_fo_membership.domain.model.Membership;

public interface SaveMembershipPort {

	void save(Membership membership);
}
