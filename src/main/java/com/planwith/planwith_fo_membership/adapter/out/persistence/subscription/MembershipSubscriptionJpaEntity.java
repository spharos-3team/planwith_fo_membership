package com.planwith.planwith_fo_membership.adapter.out.persistence.subscription;

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

import com.planwith.planwith_fo_membership.domain.model.MembershipSubscription;
import com.planwith.planwith_fo_membership.domain.model.SubscriptionStatus;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;

@Entity
@Table(
		name = "membership_subscription",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_membership_subscription_uuid",
				columnNames = "subscription_uuid"
		),
		indexes = {
				@Index(name = "idx_membership_subscription_member", columnList = "member_uuid, status"),
				@Index(name = "idx_membership_subscription_membership", columnList = "membership_uuid, status")
		}
)
class MembershipSubscriptionJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "subscription_id")
	private Long subscriptionId;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "subscription_uuid", nullable = false, unique = true, length = 36)
	private UUID subscriptionUuid;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "membership_uuid", nullable = false, length = 36)
	private UUID membershipUuid;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "member_uuid", nullable = false, length = 36)
	private UUID memberUuid;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private SubscriptionStatus status;

	@Column(name = "started_at", nullable = false)
	private Instant startedAt;

	@Column(name = "ended_at")
	private Instant endedAt;

	protected MembershipSubscriptionJpaEntity() {
	}

	static MembershipSubscriptionJpaEntity from(MembershipSubscription subscription) {
		MembershipSubscriptionJpaEntity entity = new MembershipSubscriptionJpaEntity();
		entity.apply(subscription);
		return entity;
	}

	void apply(MembershipSubscription subscription) {
		this.subscriptionUuid = subscription.subscriptionUuid().value();
		this.membershipUuid = subscription.membershipUuid().value();
		this.memberUuid = subscription.memberUuid().value();
		this.status = subscription.status();
		this.startedAt = subscription.startedAt();
		this.endedAt = subscription.endedAt();
	}

	MembershipSubscription toDomain() {
		return MembershipSubscription.restore(
				new SubscriptionUuid(subscriptionUuid),
				new MembershipUuid(membershipUuid),
				new MemberUuid(memberUuid),
				status,
				startedAt,
				endedAt
		);
	}

	UUID subscriptionUuid() {
		return subscriptionUuid;
	}
}
