package com.planwith.planwith_fo_membership.adapter.out.persistence.membership;

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

import com.planwith.planwith_fo_membership.domain.model.Membership;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;

@Entity
@Table(
		name = "membership",
		uniqueConstraints = @UniqueConstraint(name = "uk_membership_uuid", columnNames = "membership_uuid"),
		indexes = {
				@Index(name = "idx_membership_creator", columnList = "creator_uuid"),
				@Index(name = "idx_membership_admin", columnList = "admin_uuid"),
				@Index(name = "idx_membership_status", columnList = "status")
		}
)
class MembershipJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "membership_id")
	private Long membershipId;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "membership_uuid", nullable = false, unique = true, length = 36)
	private UUID membershipUuid;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "admin_uuid", length = 36)
	private UUID adminUuid;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "creator_uuid", nullable = false, length = 36)
	private UUID creatorUuid;

	@Column(name = "membership_name", nullable = false, length = 100)
	private String membershipName;

	@Column(name = "description", length = 1000)
	private String description;

	@Column(name = "monthly_price", nullable = false)
	private Integer monthlyPrice;

	@Column(name = "status", nullable = false, length = 20)
	private String status;

	@Column(name = "reject_reason", length = 100)
	private String rejectReason;

	@Column(name = "create_at", nullable = false)
	private Instant createAt;

	protected MembershipJpaEntity() {
	}

	static MembershipJpaEntity from(Membership membership) {
		MembershipJpaEntity entity = new MembershipJpaEntity();
		entity.apply(membership);
		return entity;
	}

	void apply(Membership membership) {
		this.membershipUuid = membership.membershipUuid().value();
		this.adminUuid = membership.adminUuid() == null ? null : UUID.fromString(membership.adminUuid());
		this.creatorUuid = membership.creatorUuid().value();
		this.membershipName = membership.membershipName();
		this.description = membership.description();
		this.monthlyPrice = membership.monthlyPrice();
		this.status = membership.status();
		this.rejectReason = membership.rejectReason();
		this.createAt = membership.createAt();
	}

	Membership toDomain() {
		return Membership.restore(
				new MembershipUuid(membershipUuid),
				adminUuid == null ? null : adminUuid.toString(),
				new CreatorUuid(creatorUuid),
				membershipName,
				description,
				monthlyPrice,
				status,
				rejectReason,
				createAt
		);
	}

	UUID membershipUuid() {
		return membershipUuid;
	}
}
