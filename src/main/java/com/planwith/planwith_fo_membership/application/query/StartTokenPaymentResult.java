package com.planwith.planwith_fo_membership.application.query;

import com.planwith.planwith_fo_membership.domain.model.PaymentStatus;
import com.planwith.planwith_fo_membership.domain.model.vo.PaymentUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;

public record StartTokenPaymentResult(
		PaymentUuid paymentUuid,
		SubscriptionUuid subscriptionUuid,
		long amount,
		String priceUnit,
		PaymentStatus status
) {
}
