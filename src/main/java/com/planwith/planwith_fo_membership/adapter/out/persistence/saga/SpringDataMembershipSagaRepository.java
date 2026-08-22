package com.planwith.planwith_fo_membership.adapter.out.persistence.saga;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataMembershipSagaRepository extends JpaRepository<MembershipSagaJpaEntity, Long> {

	Optional<MembershipSagaJpaEntity> findBySagaUuid(UUID sagaUuid);
}
