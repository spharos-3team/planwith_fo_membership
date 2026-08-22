package com.planwith.planwith_fo_membership.application.port.out;

import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.PaymentUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;

public interface PaymentRequestPort {

	PaymentUuid requestSubscribePayment(MemberUuid memberUuid, SubscriptionUuid subscriptionUuid);
}
