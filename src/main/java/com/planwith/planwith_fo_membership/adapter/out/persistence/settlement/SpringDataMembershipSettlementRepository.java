package com.planwith.planwith_fo_membership.adapter.out.persistence.settlement;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataMembershipSettlementRepository extends JpaRepository<MembershipSettlementJpaEntity, Long> {

	Optional<MembershipSettlementJpaEntity> findBySettlementUuid(UUID settlementUuid);
}
