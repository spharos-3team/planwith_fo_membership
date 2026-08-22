package com.planwith.planwith_fo_membership.application.port.out;

import com.planwith.planwith_fo_membership.domain.model.MembershipPayment;

public interface SavePaymentPort {

	void save(MembershipPayment payment);
}
