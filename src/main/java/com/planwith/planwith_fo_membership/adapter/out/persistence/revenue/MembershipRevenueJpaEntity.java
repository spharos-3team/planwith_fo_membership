package com.planwith.planwith_fo_membership.adapter.out.persistence.revenue;

import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
		name = "membership_revenue",
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_membership_revenue_uuid", columnNames = "revenue_uuid"),
				@UniqueConstraint(name = "uk_membership_revenue_creator", columnNames = "creator_uuid")
		}
)
class MembershipRevenueJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "revenue_id")
	private Long revenueId;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "revenue_uuid", nullable = false, unique = true, length = 36)
	private UUID revenueUuid;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "creator_uuid", nullable = false, unique = true, length = 36)
	private UUID creatorUuid;

	@Column(name = "total_revenue", nullable = false)
	private Long totalRevenue;

	@Column(name = "available_revenue", nullable = false)
	private Long availableRevenue;

	@Column(name = "settled_revenue", nullable = false)
	private Long settledRevenue;

	protected MembershipRevenueJpaEntity() {
	}

	MembershipRevenueJpaEntity(
			UUID revenueUuid,
			UUID creatorUuid,
			Long totalRevenue,
			Long availableRevenue,
			Long settledRevenue
	) {
		this.revenueUuid = revenueUuid;
		this.creatorUuid = creatorUuid;
		this.totalRevenue = totalRevenue;
		this.availableRevenue = availableRevenue;
		this.settledRevenue = settledRevenue;
	}

	Long revenueId() {
		return revenueId;
	}

	UUID revenueUuid() {
		return revenueUuid;
	}

	UUID creatorUuid() {
		return creatorUuid;
	}

	Long totalRevenue() {
		return totalRevenue;
	}

	Long availableRevenue() {
		return availableRevenue;
	}

	Long settledRevenue() {
		return settledRevenue;
	}
}
