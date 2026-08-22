package com.planwith.planwith_fo_membership.adapter.out.persistence.processed;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
		name = "processed_membership_event",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_processed_membership_event_uuid",
				columnNames = {"event_uuid"}
		),
		indexes = {
				@Index(name = "idx_processed_membership_payment", columnList = "event_type, payment_uuid"),
				@Index(name = "idx_processed_membership_settlement", columnList = "event_type, settlement_uuid")
		}
)
class ProcessedMembershipEventJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "processed_id")
	private Long processedId;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "event_uuid", nullable = false, length = 36)
	private UUID eventUuid;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "member_uuid", nullable = false, length = 36)
	private UUID memberUuid;

	@Column(name = "event_type", nullable = false, length = 50)
	private String eventType;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "payment_uuid", length = 36)
	private UUID paymentUuid;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "settlement_uuid", length = 36)
	private UUID settlementUuid;

	@Column(name = "processed_at", nullable = false)
	private Instant processedAt;

	protected ProcessedMembershipEventJpaEntity() {
	}

	static ProcessedMembershipEventJpaEntity create(
			UUID eventUuid,
			UUID memberUuid,
			String eventType,
			UUID paymentUuid,
			UUID settlementUuid,
			Instant processedAt
	) {
		ProcessedMembershipEventJpaEntity entity = new ProcessedMembershipEventJpaEntity();
		entity.eventUuid = eventUuid;
		entity.memberUuid = memberUuid;
		entity.eventType = eventType;
		entity.paymentUuid = paymentUuid;
		entity.settlementUuid = settlementUuid;
		entity.processedAt = processedAt;
		return entity;
	}
}
