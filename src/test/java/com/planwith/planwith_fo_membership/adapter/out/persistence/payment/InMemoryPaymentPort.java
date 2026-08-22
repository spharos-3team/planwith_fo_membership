package com.planwith.planwith_fo_membership.adapter.out.persistence.payment;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.planwith.planwith_fo_membership.application.port.out.LoadPaymentPort;
import com.planwith.planwith_fo_membership.application.port.out.SavePaymentPort;
import com.planwith.planwith_fo_membership.domain.model.MembershipPayment;
import com.planwith.planwith_fo_membership.domain.model.vo.PaymentUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;

public class InMemoryPaymentPort implements LoadPaymentPort, SavePaymentPort {

	private final Map<PaymentUuid, MembershipPayment> payments = new HashMap<>();

	@Override
	public void save(MembershipPayment payment) {
		payments.put(payment.paymentUuid(), payment);
	}

	@Override
	public Optional<MembershipPayment> findByUuid(PaymentUuid paymentUuid) {
		return Optional.ofNullable(payments.get(paymentUuid));
	}

	@Override
	public Optional<MembershipPayment> findLatestBySubscription(SubscriptionUuid subscriptionUuid) {
		return payments.values().stream()
				.filter(payment -> payment.subscriptionUuid().equals(subscriptionUuid))
				.findFirst();
	}
}
