package com.planwith.planwith_fo_membership.adapter.out.persistence.payment;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataMembershipPaymentRepository extends JpaRepository<MembershipPaymentJpaEntity, Long> {

	Optional<MembershipPaymentJpaEntity> findByPaymentUuid(UUID paymentUuid);

	Optional<MembershipPaymentJpaEntity> findFirstBySubscriptionUuidOrderByPaymentIdDesc(UUID subscriptionUuid);
}
