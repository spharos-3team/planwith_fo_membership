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

import com.planwith.planwith_fo_membership.domain.model.MembershipRevenue;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.RevenueUuid;

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

	@Column(name = "reserved_revenue", nullable = false)
	private Long reservedRevenue = 0L;

	@Column(name = "settled_revenue", nullable = false)
	private Long settledRevenue;

	protected MembershipRevenueJpaEntity() {
	}

	static MembershipRevenueJpaEntity from(MembershipRevenue revenue) {
		MembershipRevenueJpaEntity entity = new MembershipRevenueJpaEntity();
		entity.apply(revenue);
		return entity;
	}

	void apply(MembershipRevenue revenue) {
		this.revenueUuid = revenue.revenueUuid().value();
		this.creatorUuid = revenue.creatorUuid().value();
		this.totalRevenue = revenue.totalRevenue();
		this.availableRevenue = revenue.availableRevenue();
		this.reservedRevenue = revenue.reservedRevenue();
		this.settledRevenue = revenue.settledRevenue();
	}

	MembershipRevenue toDomain() {
		return MembershipRevenue.restore(
				new RevenueUuid(revenueUuid),
				new CreatorUuid(creatorUuid),
				totalRevenue,
				availableRevenue,
				reservedRevenue == null ? 0L : reservedRevenue,
				settledRevenue
		);
	}
}
