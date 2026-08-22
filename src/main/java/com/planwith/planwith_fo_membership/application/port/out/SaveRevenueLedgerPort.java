package com.planwith.planwith_fo_membership.application.port.out;

import com.planwith.planwith_fo_membership.domain.model.MembershipRevenueLedger;

public interface SaveRevenueLedgerPort {

	void save(MembershipRevenueLedger ledger);
}
