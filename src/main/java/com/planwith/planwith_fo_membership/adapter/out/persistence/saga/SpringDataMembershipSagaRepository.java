package com.planwith.planwith_fo_membership.adapter.out.persistence.saga;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.planwith.planwith_fo_membership.domain.model.MembershipSagaStatus;

interface SpringDataMembershipSagaRepository extends JpaRepository<MembershipSagaJpaEntity, Long> {

	Optional<MembershipSagaJpaEntity> findBySagaUuid(UUID sagaUuid);

	Optional<MembershipSagaJpaEntity> findFirstByMemberUuidAndCreatorUuidAndStatusInOrderByUpdatedAtDesc(
			UUID memberUuid,
			UUID creatorUuid,
			Collection<MembershipSagaStatus> statuses
	);

	Optional<MembershipSagaJpaEntity> findFirstByPaymentUuidOrderByUpdatedAtDesc(UUID paymentUuid);
}
