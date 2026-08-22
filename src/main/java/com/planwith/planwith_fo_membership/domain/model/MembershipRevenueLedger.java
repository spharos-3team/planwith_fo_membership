package com.planwith.planwith_fo_membership.domain.model;

import java.time.Instant;
import java.util.Objects;

import com.planwith.planwith_fo_membership.domain.exception.InvalidRevenueException;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.LedgerUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.PaymentUuid;
import com.planwith.planwith_fo_membership.domain.service.RevenueSharePolicy.RevenueShareResult;

public final class MembershipRevenueLedger {

	private final LedgerUuid ledgerUuid;
	private final PaymentUuid paymentUuid;
	private final CreatorUuid creatorUuid;
	private final long tokenAmount;
	private final long grossKrw;
	private final long companyShareKrw;
	private final long creatorShareKrw;
	private final Instant recordedAt;

	private MembershipRevenueLedger(
			LedgerUuid ledgerUuid,
			PaymentUuid paymentUuid,
			CreatorUuid creatorUuid,
			long tokenAmount,
			long grossKrw,
			long companyShareKrw,
			long creatorShareKrw,
			Instant recordedAt
	) {
		this.ledgerUuid = Objects.requireNonNull(ledgerUuid, "Ledger UUID is required.");
		this.paymentUuid = Objects.requireNonNull(paymentUuid, "Payment UUID is required.");
		this.creatorUuid = Objects.requireNonNull(creatorUuid, "Creator UUID is required.");
		if (tokenAmount <= 0 || grossKrw <= 0 || companyShareKrw < 0 || creatorShareKrw < 0) {
			throw new InvalidRevenueException("수익 원장 금액이 올바르지 않습니다.");
		}
		if (companyShareKrw + creatorShareKrw != grossKrw) {
			throw new InvalidRevenueException("회사 수익과 Creator 수익의 합은 총 환산 금액과 같아야 합니다.");
		}
		this.tokenAmount = tokenAmount;
		this.grossKrw = grossKrw;
		this.companyShareKrw = companyShareKrw;
		this.creatorShareKrw = creatorShareKrw;
		this.recordedAt = Objects.requireNonNull(recordedAt, "Recorded at is required.");
	}

	public static MembershipRevenueLedger recorded(
			LedgerUuid ledgerUuid,
			PaymentUuid paymentUuid,
			CreatorUuid creatorUuid,
			RevenueShareResult share,
			Instant recordedAt
	) {
		return new MembershipRevenueLedger(
				ledgerUuid,
				paymentUuid,
				creatorUuid,
				share.tokenAmount(),
				share.grossKrw(),
				share.companyShareKrw(),
				share.creatorShareKrw(),
				recordedAt
		);
	}

	public static MembershipRevenueLedger restore(
			LedgerUuid ledgerUuid,
			PaymentUuid paymentUuid,
			CreatorUuid creatorUuid,
			long tokenAmount,
			long grossKrw,
			long companyShareKrw,
			long creatorShareKrw,
			Instant recordedAt
	) {
		return new MembershipRevenueLedger(
				ledgerUuid,
				paymentUuid,
				creatorUuid,
				tokenAmount,
				grossKrw,
				companyShareKrw,
				creatorShareKrw,
				recordedAt
		);
	}

	public LedgerUuid ledgerUuid() {
		return ledgerUuid;
	}

	public PaymentUuid paymentUuid() {
		return paymentUuid;
	}

	public CreatorUuid creatorUuid() {
		return creatorUuid;
	}

	public long tokenAmount() {
		return tokenAmount;
	}

	public long grossKrw() {
		return grossKrw;
	}

	public long companyShareKrw() {
		return companyShareKrw;
	}

	public long creatorShareKrw() {
		return creatorShareKrw;
	}

	public Instant recordedAt() {
		return recordedAt;
	}
}
