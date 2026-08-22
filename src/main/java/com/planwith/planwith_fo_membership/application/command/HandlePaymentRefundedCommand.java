package com.planwith.planwith_fo_membership.application.command;

import java.time.Instant;
import java.util.UUID;

import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.PaymentUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;

public record HandlePaymentRefundedCommand(
		UUID eventUuid,
		MemberUuid memberUuid,
		PaymentUuid paymentUuid,
		SubscriptionUuid subscriptionUuid,
		Instant refundedAt
) {
}
