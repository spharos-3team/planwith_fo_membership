package com.planwith.planwith_fo_membership.application.command;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.PaymentUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;

public record HandleTokenDeductionFailedCommand(
		UUID eventUuid,
		MemberUuid memberUuid,
		PaymentUuid paymentUuid,
		SubscriptionUuid subscriptionUuid,
		Instant failedAt
) {

	public HandleTokenDeductionFailedCommand {
		Objects.requireNonNull(eventUuid, "Event UUID is required.");
		Objects.requireNonNull(memberUuid, "Member UUID is required.");
		Objects.requireNonNull(paymentUuid, "Payment UUID is required.");
	}
}
