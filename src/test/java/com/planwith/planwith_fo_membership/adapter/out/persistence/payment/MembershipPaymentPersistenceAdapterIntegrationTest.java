package com.planwith.planwith_fo_membership.adapter.out.persistence.payment;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_membership.application.port.out.LoadPaymentPort;
import com.planwith.planwith_fo_membership.application.port.out.SavePaymentPort;
import com.planwith.planwith_fo_membership.domain.model.MembershipPayment;
import com.planwith.planwith_fo_membership.domain.model.PaymentStatus;
import com.planwith.planwith_fo_membership.domain.model.vo.PaymentUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MembershipPaymentPersistenceAdapterIntegrationTest {

	@Autowired
	private SavePaymentPort savePaymentPort;

	@Autowired
	private LoadPaymentPort loadPaymentPort;

	@Test
	void saveAndLoadPaymentStatusTransition() {
		PaymentUuid paymentUuid = PaymentUuid.from("44444444-4444-4444-4444-444444444444");
		SubscriptionUuid subscriptionUuid = SubscriptionUuid.from("55555555-5555-5555-5555-555555555555");
		Instant paidAt = Instant.parse("2026-08-22T00:10:00Z");

		savePaymentPort.save(MembershipPayment.ready(paymentUuid, subscriptionUuid, 12900L));
		MembershipPayment ready = loadPaymentPort.findByUuid(paymentUuid).orElseThrow();
		assertThat(ready.paymentStatus()).isEqualTo(PaymentStatus.READY);
		assertThat(ready.paidAt()).isNull();

		savePaymentPort.save(ready.succeed(paidAt));
		MembershipPayment succeeded = loadPaymentPort.findLatestBySubscription(subscriptionUuid).orElseThrow();
		assertThat(succeeded.paymentUuid()).isEqualTo(paymentUuid);
		assertThat(succeeded.paymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
		assertThat(succeeded.amount()).isEqualTo(12900L);
		assertThat(succeeded.paidAt()).isEqualTo(paidAt);
	}
}
