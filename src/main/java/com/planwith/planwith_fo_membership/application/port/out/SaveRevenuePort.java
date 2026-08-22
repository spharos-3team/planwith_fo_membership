package com.planwith.planwith_fo_membership.application.port.out;

import com.planwith.planwith_fo_membership.domain.model.MembershipRevenue;

public interface SaveRevenuePort {

	void save(MembershipRevenue revenue);
}
