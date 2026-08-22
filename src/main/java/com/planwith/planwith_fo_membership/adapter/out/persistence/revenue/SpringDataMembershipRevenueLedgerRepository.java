package com.planwith.planwith_fo_membership.adapter.out.persistence.revenue;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataMembershipRevenueLedgerRepository extends JpaRepository<MembershipRevenueLedgerJpaEntity, Long> {

	Optional<MembershipRevenueLedgerJpaEntity> findByPaymentUuid(UUID paymentUuid);
}
