package com.planwith.planwith_fo_membership.adapter.out.persistence.revenue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.planwith.planwith_fo_membership.application.port.out.LoadRevenueLedgerPort;
import com.planwith.planwith_fo_membership.application.port.out.SaveRevenueLedgerPort;
import com.planwith.planwith_fo_membership.domain.model.MembershipRevenueLedger;
import com.planwith.planwith_fo_membership.domain.model.vo.PaymentUuid;

public class InMemoryRevenueLedgerPort implements LoadRevenueLedgerPort, SaveRevenueLedgerPort {

	private final Map<PaymentUuid, MembershipRevenueLedger> ledgers = new HashMap<>();

	@Override
	public Optional<MembershipRevenueLedger> findByPaymentUuid(PaymentUuid paymentUuid) {
		return Optional.ofNullable(ledgers.get(paymentUuid));
	}

	@Override
	public void save(MembershipRevenueLedger ledger) {
		ledgers.putIfAbsent(ledger.paymentUuid(), ledger);
	}
}
