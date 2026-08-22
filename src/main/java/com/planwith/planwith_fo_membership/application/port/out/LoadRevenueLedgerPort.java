package com.planwith.planwith_fo_membership.application.port.out;

import java.util.Optional;

import com.planwith.planwith_fo_membership.domain.model.MembershipRevenueLedger;
import com.planwith.planwith_fo_membership.domain.model.vo.PaymentUuid;

public interface LoadRevenueLedgerPort {

	Optional<MembershipRevenueLedger> findByPaymentUuid(PaymentUuid paymentUuid);
}
