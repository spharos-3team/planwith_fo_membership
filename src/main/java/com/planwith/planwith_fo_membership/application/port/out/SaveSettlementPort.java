package com.planwith.planwith_fo_membership.application.port.out;

import com.planwith.planwith_fo_membership.domain.model.MembershipSettlement;

public interface SaveSettlementPort {

	void save(MembershipSettlement settlement);
}
