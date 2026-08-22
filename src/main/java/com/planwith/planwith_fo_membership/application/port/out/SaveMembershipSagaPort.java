package com.planwith.planwith_fo_membership.application.port.out;

import com.planwith.planwith_fo_membership.domain.model.MembershipSaga;

public interface SaveMembershipSagaPort {

	void save(MembershipSaga saga);
}
