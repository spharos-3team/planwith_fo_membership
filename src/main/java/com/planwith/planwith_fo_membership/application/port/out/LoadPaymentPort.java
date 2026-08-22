package com.planwith.planwith_fo_membership.application.port.out;

import java.util.Optional;

import com.planwith.planwith_fo_membership.domain.model.MembershipPayment;
import com.planwith.planwith_fo_membership.domain.model.vo.PaymentUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;

public interface LoadPaymentPort {

	Optional<MembershipPayment> findByUuid(PaymentUuid paymentUuid);

	Optional<MembershipPayment> findLatestBySubscription(SubscriptionUuid subscriptionUuid);
}
