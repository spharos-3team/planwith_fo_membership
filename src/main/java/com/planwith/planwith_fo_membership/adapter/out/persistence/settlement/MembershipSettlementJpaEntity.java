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

import com.planwith.planwith_fo_membership.domain.model.MembershipSettlement;
import com.planwith.planwith_fo_membership.domain.model.SettlementStatus;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.RevenueUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SettlementUuid;

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

	static MembershipSettlementJpaEntity from(MembershipSettlement settlement) {
		MembershipSettlementJpaEntity entity = new MembershipSettlementJpaEntity();
		entity.apply(settlement);
		return entity;
	}

	void apply(MembershipSettlement settlement) {
		this.settlementUuid = settlement.settlementUuid().value();
		this.creatorUuid = settlement.creatorUuid().value();
		this.revenueUuid = settlement.revenueUuid().value();
		this.settlementAmount = settlement.settlementAmount();
		this.settlementStatus = settlement.settlementStatus();
		this.requestedAt = settlement.requestedAt();
		this.approvedAt = settlement.approvedAt();
		this.paidAt = settlement.paidAt();
		this.rejectReason = settlement.rejectReason();
	}

	MembershipSettlement toDomain() {
		return MembershipSettlement.restore(
				new SettlementUuid(settlementUuid),
				new CreatorUuid(creatorUuid),
				new RevenueUuid(revenueUuid),
				settlementAmount,
				settlementStatus,
				requestedAt,
				approvedAt,
				paidAt,
				rejectReason
		);
	}
}
