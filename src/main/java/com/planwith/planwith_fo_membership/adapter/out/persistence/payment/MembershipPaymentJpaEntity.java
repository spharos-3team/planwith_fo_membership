package com.planwith.planwith_fo_membership.adapter.out.persistence.payment;

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

import com.planwith.planwith_fo_membership.domain.model.PaymentStatus;

@Entity
@Table(
		name = "membership_payment",
		uniqueConstraints = @UniqueConstraint(name = "uk_membership_payment_uuid", columnNames = "payment_uuid"),
		indexes = @Index(name = "idx_membership_payment_subscription", columnList = "subscription_uuid, paid_at")
)
class MembershipPaymentJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "payment_id")
	private Long paymentId;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "payment_uuid", nullable = false, unique = true, length = 36)
	private UUID paymentUuid;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "subscription_uuid", nullable = false, length = 36)
	private UUID subscriptionUuid;

	@Column(name = "amount", nullable = false)
	private Long amount;

	@Enumerated(EnumType.STRING)
	@Column(name = "payment_status", nullable = false, length = 20)
	private PaymentStatus paymentStatus;

	@Column(name = "paid_at")
	private Instant paidAt;

	protected MembershipPaymentJpaEntity() {
	}

	MembershipPaymentJpaEntity(
			UUID paymentUuid,
			UUID subscriptionUuid,
			Long amount,
			PaymentStatus paymentStatus,
			Instant paidAt
	) {
		this.paymentUuid = paymentUuid;
		this.subscriptionUuid = subscriptionUuid;
		this.amount = amount;
		this.paymentStatus = paymentStatus;
		this.paidAt = paidAt;
	}

	Long paymentId() {
		return paymentId;
	}

	UUID paymentUuid() {
		return paymentUuid;
	}

	UUID subscriptionUuid() {
		return subscriptionUuid;
	}

	Long amount() {
		return amount;
	}

	PaymentStatus paymentStatus() {
		return paymentStatus;
	}

	Instant paidAt() {
		return paidAt;
	}
}
