package com.planwith.planwith_fo_membership.adapter.out.persistence.membership;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataMembershipRepository extends JpaRepository<MembershipJpaEntity, Long> {

	Optional<MembershipJpaEntity> findByMembershipUuid(UUID membershipUuid);
}
