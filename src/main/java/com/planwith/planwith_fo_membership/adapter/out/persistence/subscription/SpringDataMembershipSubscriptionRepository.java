package com.planwith.planwith_fo_membership.adapter.out.persistence.subscription;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import java.time.Instant;

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

	@Query("""
			select
				subscription.subscriptionUuid as subscriptionUuid,
				subscription.membershipUuid as membershipUuid,
				membership.creatorUuid as creatorUuid,
				membership.membershipName as membershipName,
				membership.monthlyPrice as monthlyPrice,
				subscription.status as status,
				subscription.startedAt as startedAt,
				subscription.endedAt as endedAt
			from MembershipSubscriptionJpaEntity subscription, MembershipJpaEntity membership
			where subscription.membershipUuid = membership.membershipUuid
				and subscription.memberUuid = :memberUuid
				and subscription.status = :status
			order by subscription.startedAt desc
			""")
	List<JoinedMembershipRow> findJoinedByMemberUuid(
			@Param("memberUuid") UUID memberUuid,
			@Param("status") SubscriptionStatus status
	);

	@Query("""
			select subscription
			from MembershipSubscriptionJpaEntity subscription, MembershipJpaEntity membership
			where subscription.membershipUuid = membership.membershipUuid
				and membership.creatorUuid = :creatorUuid
				and subscription.status = :status
			order by subscription.startedAt desc
			""")
	List<MembershipSubscriptionJpaEntity> findActiveByCreatorUuid(
			@Param("creatorUuid") UUID creatorUuid,
			@Param("status") SubscriptionStatus status
	);

	List<MembershipSubscriptionJpaEntity> findByStatusAndStartedAtLessThanEqual(
			SubscriptionStatus status,
			Instant startedAt
	);

	@Query("""
			select membership.creatorUuid as creatorUuid, count(subscription) as subscriberCount
			from MembershipSubscriptionJpaEntity subscription, MembershipJpaEntity membership
			where subscription.membershipUuid = membership.membershipUuid
				and membership.creatorUuid in :creatorUuids
				and subscription.status = :status
			group by membership.creatorUuid
			""")
	List<CreatorSubscriberCountRow> countActiveByCreatorUuids(
			@Param("creatorUuids") List<UUID> creatorUuids,
			@Param("status") SubscriptionStatus status
	);
}
