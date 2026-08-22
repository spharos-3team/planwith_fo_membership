package com.planwith.planwith_fo_membership.adapter.out.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_membership.application.port.out.PaymentRequestPort;
import com.planwith.planwith_fo_membership.domain.exception.UnsupportedMembershipOperationException;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.PaymentUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;

@Component
public class StubPaymentRequestAdapter implements PaymentRequestPort {

	private static final Logger log = LoggerFactory.getLogger(StubPaymentRequestAdapter.class);

	@Override
	public PaymentUuid requestSubscribePayment(MemberUuid memberUuid, SubscriptionUuid subscriptionUuid) {
		log.debug(
				"StubPaymentRequestAdapter : requestSubscribePayment : Payment 서비스 요청은 후속 이슈에서 구현한다 - memberUuid={}, subscriptionUuid={}",
				memberUuid,
				subscriptionUuid
		);
		throw new UnsupportedMembershipOperationException("Payment 서비스 결제 요청은 후속 이슈에서 구현한다.");
	}
}
