package com.planwith.planwith_fo_membership.adapter.out.persistence.payment;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_membership.application.port.out.LoadPaymentPort;
import com.planwith.planwith_fo_membership.application.port.out.SavePaymentPort;
import com.planwith.planwith_fo_membership.domain.model.MembershipPayment;
import com.planwith.planwith_fo_membership.domain.model.vo.PaymentUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;

@Component
public class MembershipPaymentPersistenceAdapter implements LoadPaymentPort, SavePaymentPort {

	private static final Logger log = LoggerFactory.getLogger(MembershipPaymentPersistenceAdapter.class);

	private final SpringDataMembershipPaymentRepository repository;

	public MembershipPaymentPersistenceAdapter(SpringDataMembershipPaymentRepository repository) {
		this.repository = repository;
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<MembershipPayment> findByUuid(PaymentUuid paymentUuid) {
		return repository.findByPaymentUuid(paymentUuid.value())
				.map(MembershipPaymentJpaEntity::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<MembershipPayment> findLatestBySubscription(SubscriptionUuid subscriptionUuid) {
		return repository.findFirstBySubscriptionUuidOrderByPaymentIdDesc(subscriptionUuid.value())
				.map(MembershipPaymentJpaEntity::toDomain);
	}

	@Override
	@Transactional
	public void save(MembershipPayment payment) {
		MembershipPaymentJpaEntity entity = repository.findByPaymentUuid(payment.paymentUuid().value())
				.orElseGet(() -> MembershipPaymentJpaEntity.from(payment));
		entity.apply(payment);
		repository.save(entity);
		log.debug(
				"MembershipPaymentPersistenceAdapter : save : 멤버십 결제 저장 - paymentUuid={}, status={}",
				payment.paymentUuid(),
				payment.paymentStatus()
		);
	}
}
