package com.planwith.planwith_fo_membership.adapter.out.persistence.subscription;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.planwith.planwith_fo_membership.domain.model.SubscriptionStatus;

interface SpringDataMembershipSubscriptionRepository extends JpaRepository<MembershipSubscriptionJpaEntity, Long> {

	Optional<MembershipSubscriptionJpaEntity> findBySubscriptionUuid(UUID subscriptionUuid);

	Optional<MembershipSubscriptionJpaEntity> findFirstByMemberUuidAndMembershipUuidAndStatusOrderByStartedAtDesc(
			UUID memberUuid,
			UUID membershipUuid,
			SubscriptionStatus status
	);

	@Query("""
			select subscription
			from MembershipSubscriptionJpaEntity subscription, MembershipJpaEntity membership
			where subscription.membershipUuid = membership.membershipUuid
				and subscription.memberUuid = :memberUuid
				and membership.creatorUuid = :creatorUuid
				and subscription.status = :status
			order by subscription.startedAt desc
			""")
	Optional<MembershipSubscriptionJpaEntity> findCurrentByMemberAndCreator(
			@Param("memberUuid") UUID memberUuid,
			@Param("creatorUuid") UUID creatorUuid,
			@Param("status") SubscriptionStatus status
	);
}
