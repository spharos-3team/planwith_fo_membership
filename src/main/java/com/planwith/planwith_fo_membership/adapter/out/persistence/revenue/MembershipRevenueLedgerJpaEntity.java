package com.planwith.planwith_fo_membership.adapter.out.persistence.revenue;

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

import com.planwith.planwith_fo_membership.domain.model.MembershipRevenueLedger;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.LedgerUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.PaymentUuid;

@Entity
@Table(
		name = "membership_revenue_ledger",
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_membership_revenue_ledger_uuid", columnNames = "ledger_uuid"),
				@UniqueConstraint(name = "uk_membership_revenue_ledger_payment", columnNames = "payment_uuid")
		},
		indexes = @Index(name = "idx_membership_revenue_ledger_creator", columnList = "creator_uuid")
)
class MembershipRevenueLedgerJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ledger_id")
	private Long ledgerId;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "ledger_uuid", nullable = false, unique = true, length = 36)
	private UUID ledgerUuid;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "payment_uuid", nullable = false, unique = true, length = 36)
	private UUID paymentUuid;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "creator_uuid", nullable = false, length = 36)
	private UUID creatorUuid;

	@Column(name = "token_amount", nullable = false)
	private Long tokenAmount;

	@Column(name = "gross_krw", nullable = false)
	private Long grossKrw;

	@Column(name = "company_share_krw", nullable = false)
	private Long companyShareKrw;

	@Column(name = "creator_share_krw", nullable = false)
	private Long creatorShareKrw;

	@Column(name = "recorded_at", nullable = false)
	private Instant recordedAt;

	protected MembershipRevenueLedgerJpaEntity() {
	}

	static MembershipRevenueLedgerJpaEntity from(MembershipRevenueLedger ledger) {
		MembershipRevenueLedgerJpaEntity entity = new MembershipRevenueLedgerJpaEntity();
		entity.apply(ledger);
		return entity;
	}

	void apply(MembershipRevenueLedger ledger) {
		this.ledgerUuid = ledger.ledgerUuid().value();
		this.paymentUuid = ledger.paymentUuid().value();
		this.creatorUuid = ledger.creatorUuid().value();
		this.tokenAmount = ledger.tokenAmount();
		this.grossKrw = ledger.grossKrw();
		this.companyShareKrw = ledger.companyShareKrw();
		this.creatorShareKrw = ledger.creatorShareKrw();
		this.recordedAt = ledger.recordedAt();
	}

	MembershipRevenueLedger toDomain() {
		return MembershipRevenueLedger.restore(
				new LedgerUuid(ledgerUuid),
				new PaymentUuid(paymentUuid),
				new CreatorUuid(creatorUuid),
				tokenAmount,
				grossKrw,
				companyShareKrw,
				creatorShareKrw,
				recordedAt
		);
	}
}
