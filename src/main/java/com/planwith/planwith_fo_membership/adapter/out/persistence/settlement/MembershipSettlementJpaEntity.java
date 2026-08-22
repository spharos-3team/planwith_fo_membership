package com.planwith.planwith_fo_membership.adapter.out.persistence.settlement;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.planwith.planwith_fo_membership.domain.model.SettlementStatus;

@Entity
@Table(
		name = "membership_settlement",
		uniqueConstraints = @UniqueConstraint(name = "uk_membership_settlement_uuid", columnNames = "settlement_uuid"),
		indexes = {
				@Index(name = "idx_membership_settlement_creator", columnList = "creator_uuid, settlement_status"),
				@Index(name = "idx_membership_settlement_revenue", columnList = "revenue_uuid")
		}
)
class MembershipSettlementJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "settlement_id")
	private Long settlementId;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "settlement_uuid", nullable = false, unique = true, length = 36)
	private UUID settlementUuid;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "creator_uuid", nullable = false, length = 36)
	private UUID creatorUuid;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "revenue_uuid", nullable = false, length = 36)
	private UUID revenueUuid;

	@Column(name = "settlement_amount", nullable = false)
	private Long settlementAmount;

	@Enumerated(EnumType.STRING)
	@Column(name = "settlement_status", nullable = false, length = 20)
	private SettlementStatus settlementStatus;

	@Column(name = "requested_at", nullable = false)
	private Instant requestedAt;

	@Column(name = "approved_at")
	private Instant approvedAt;

	@Column(name = "paid_at")
	private Instant paidAt;

	@Column(name = "reject_reason", length = 500)
	private String rejectReason;

	protected MembershipSettlementJpaEntity() {
	}

	MembershipSettlementJpaEntity(
			UUID settlementUuid,
			UUID creatorUuid,
			UUID revenueUuid,
			Long settlementAmount,
			SettlementStatus settlementStatus,
			Instant requestedAt,
			Instant approvedAt,
			Instant paidAt,
			String rejectReason
	) {
		this.settlementUuid = settlementUuid;
		this.creatorUuid = creatorUuid;
		this.revenueUuid = revenueUuid;
		this.settlementAmount = settlementAmount;
		this.settlementStatus = settlementStatus;
		this.requestedAt = requestedAt;
		this.approvedAt = approvedAt;
		this.paidAt = paidAt;
		this.rejectReason = rejectReason;
	}

	Long settlementId() {
		return settlementId;
	}

	UUID settlementUuid() {
		return settlementUuid;
	}

	UUID creatorUuid() {
		return creatorUuid;
	}

	UUID revenueUuid() {
		return revenueUuid;
	}

	Long settlementAmount() {
		return settlementAmount;
	}

	SettlementStatus settlementStatus() {
		return settlementStatus;
	}

	Instant requestedAt() {
		return requestedAt;
	}

	Instant approvedAt() {
		return approvedAt;
	}

	Instant paidAt() {
		return paidAt;
	}

	String rejectReason() {
		return rejectReason;
	}
}
