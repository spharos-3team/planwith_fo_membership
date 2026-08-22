package com.planwith.planwith_fo_membership.adapter.out.persistence.saga;

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

import com.planwith.planwith_fo_membership.domain.model.MembershipSaga;
import com.planwith.planwith_fo_membership.domain.model.MembershipSagaStatus;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.PaymentUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;

@Entity
@Table(
		name = "membership_saga",
		indexes = @Index(name = "idx_membership_saga_member_creator_status", columnList = "member_uuid, creator_uuid, status")
)
class MembershipSagaJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "saga_id")
	private Long sagaId;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "saga_uuid", nullable = false, unique = true, length = 36)
	private UUID sagaUuid;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "member_uuid", nullable = false, length = 36)
	private UUID memberUuid;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "creator_uuid", nullable = false, length = 36)
	private UUID creatorUuid;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "subscription_uuid", nullable = false, length = 36)
	private UUID subscriptionUuid;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "payment_uuid", length = 36)
	private UUID paymentUuid;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 30)
	private MembershipSagaStatus status;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected MembershipSagaJpaEntity() {
	}

	static MembershipSagaJpaEntity from(MembershipSaga saga) {
		MembershipSagaJpaEntity entity = new MembershipSagaJpaEntity();
		entity.apply(saga);
		return entity;
	}

	void apply(MembershipSaga saga) {
		this.sagaUuid = saga.sagaUuid();
		this.memberUuid = saga.memberUuid().value();
		this.creatorUuid = saga.creatorUuid().value();
		this.subscriptionUuid = saga.subscriptionUuid().value();
		this.paymentUuid = saga.paymentUuid() == null ? null : saga.paymentUuid().value();
		this.status = saga.status();
		this.updatedAt = saga.updatedAt();
	}

	MembershipSaga toDomain() {
		return MembershipSaga.restore(
				sagaUuid,
				new MemberUuid(memberUuid),
				new CreatorUuid(creatorUuid),
				new SubscriptionUuid(subscriptionUuid),
				paymentUuid == null ? null : new PaymentUuid(paymentUuid),
				status,
				updatedAt
		);
	}

	UUID sagaUuid() {
		return sagaUuid;
	}
}
