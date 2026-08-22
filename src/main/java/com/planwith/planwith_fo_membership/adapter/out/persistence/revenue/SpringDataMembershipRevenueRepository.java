package com.planwith.planwith_fo_membership.adapter.out.persistence.revenue;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataMembershipRevenueRepository extends JpaRepository<MembershipRevenueJpaEntity, Long> {

	Optional<MembershipRevenueJpaEntity> findByRevenueUuid(UUID revenueUuid);

	Optional<MembershipRevenueJpaEntity> findByCreatorUuid(UUID creatorUuid);
}
