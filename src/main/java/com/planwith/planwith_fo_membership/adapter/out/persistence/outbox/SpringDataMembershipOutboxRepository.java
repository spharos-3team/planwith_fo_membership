package com.planwith.planwith_fo_membership.adapter.out.persistence.outbox;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataMembershipOutboxRepository extends JpaRepository<MembershipOutboxJpaEntity, Long> {

	boolean existsByEventUuid(UUID eventUuid);

	Optional<MembershipOutboxJpaEntity> findByEventUuid(UUID eventUuid);

	@Query("""
			select outbox
			from MembershipOutboxJpaEntity outbox
			where outbox.publishedAt is null
				and (outbox.nextRetryAt is null or outbox.nextRetryAt <= :now)
			order by outbox.occurredAt asc
			""")
	List<MembershipOutboxJpaEntity> findDueUnpublished(@Param("now") Instant now, Pageable pageable);
}
