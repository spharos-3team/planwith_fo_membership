package com.planwith.planwith_fo_membership.domain.model;

import java.time.Instant;
import java.util.Objects;

import com.planwith.planwith_fo_membership.domain.exception.InvalidSettlementStateException;
import com.planwith.planwith_fo_membership.domain.model.vo.AdminUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.RevenueUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SettlementUuid;
import com.planwith.planwith_fo_membership.domain.service.SettlementPolicy;

public final class MembershipSettlement {

	private static final int REJECT_REASON_MAX_LENGTH = 500;

	private final SettlementUuid settlementUuid;
	private final CreatorUuid creatorUuid;
	private final RevenueUuid revenueUuid;
	private final long settlementAmount;
	private final SettlementStatus settlementStatus;
	private final Instant requestedAt;
	private final Instant approvedAt;
	private final Instant paidAt;
	private final String rejectReason;
	private final AdminUuid processedBy;

	private MembershipSettlement(
			SettlementUuid settlementUuid,
			CreatorUuid creatorUuid,
			RevenueUuid revenueUuid,
			long settlementAmount,
			SettlementStatus settlementStatus,
			Instant requestedAt,
			Instant approvedAt,
			Instant paidAt,
			String rejectReason,
			AdminUuid processedBy
	) {
		this.settlementUuid = Objects.requireNonNull(settlementUuid, "Settlement UUID is required.");
		this.creatorUuid = Objects.requireNonNull(creatorUuid, "Creator UUID is required.");
		this.revenueUuid = Objects.requireNonNull(revenueUuid, "Revenue UUID is required.");
		this.settlementAmount = requireAmount(settlementAmount);
		this.settlementStatus = Objects.requireNonNull(settlementStatus, "Settlement status is required.");
		this.requestedAt = Objects.requireNonNull(requestedAt, "Requested at is required.");
		this.approvedAt = approvedAt;
		this.paidAt = paidAt;
		this.rejectReason = rejectReason;
		this.processedBy = processedBy;
	}

	public static MembershipSettlement request(
			SettlementUuid settlementUuid,
			CreatorUuid creatorUuid,
			RevenueUuid revenueUuid,
			long settlementAmount,
			Instant requestedAt
	) {
		return new MembershipSettlement(
				settlementUuid,
				creatorUuid,
				revenueUuid,
				settlementAmount,
				SettlementStatus.REQUESTED,
				requestedAt,
				null,
				null,
				null,
				null
		);
	}

	public static MembershipSettlement restore(
			SettlementUuid settlementUuid,
			CreatorUuid creatorUuid,
			RevenueUuid revenueUuid,
			long settlementAmount,
			SettlementStatus settlementStatus,
			Instant requestedAt,
			Instant approvedAt,
			Instant paidAt,
			String rejectReason,
			AdminUuid processedBy
	) {
		return new MembershipSettlement(
				settlementUuid,
				creatorUuid,
				revenueUuid,
				settlementAmount,
				settlementStatus,
				requestedAt,
				approvedAt,
				paidAt,
				rejectReason,
				processedBy
		);
	}

	public MembershipSettlement approve(AdminUuid adminUuid, Instant approvedAt) {
		SettlementPolicy.requireCanApprove(settlementStatus);
		return new MembershipSettlement(
				settlementUuid,
				creatorUuid,
				revenueUuid,
				settlementAmount,
				SettlementStatus.APPROVED,
				requestedAt,
				Objects.requireNonNull(approvedAt, "Approved at is required."),
				null,
				null,
				Objects.requireNonNull(adminUuid, "Admin UUID is required.")
		);
	}

	public MembershipSettlement reject(AdminUuid adminUuid, String reason) {
		SettlementPolicy.requireCanReject(settlementStatus);
		return new MembershipSettlement(
				settlementUuid,
				creatorUuid,
				revenueUuid,
				settlementAmount,
				SettlementStatus.REJECTED,
				requestedAt,
				null,
				null,
				requireRejectReason(reason),
				Objects.requireNonNull(adminUuid, "Admin UUID is required.")
		);
	}

	public MembershipSettlement pay(AdminUuid adminUuid, Instant paidAt) {
		SettlementPolicy.requireCanPay(settlementStatus);
		return new MembershipSettlement(
				settlementUuid,
				creatorUuid,
				revenueUuid,
				settlementAmount,
				SettlementStatus.PAID,
				requestedAt,
				approvedAt,
				Objects.requireNonNull(paidAt, "Paid at is required."),
				null,
				Objects.requireNonNull(adminUuid, "Admin UUID is required.")
		);
	}

	public SettlementUuid settlementUuid() {
		return settlementUuid;
	}

	public CreatorUuid creatorUuid() {
		return creatorUuid;
	}

	public RevenueUuid revenueUuid() {
		return revenueUuid;
	}

	public long settlementAmount() {
		return settlementAmount;
	}

	public SettlementStatus settlementStatus() {
		return settlementStatus;
	}

	public Instant requestedAt() {
		return requestedAt;
	}

	public Instant approvedAt() {
		return approvedAt;
	}

	public Instant paidAt() {
		return paidAt;
	}

	public String rejectReason() {
		return rejectReason;
	}

	public AdminUuid processedBy() {
		return processedBy;
	}

	private static long requireAmount(long settlementAmount) {
		if (settlementAmount <= 0) {
			throw new InvalidSettlementStateException("정산 금액은 0보다 커야 합니다.");
		}
		return settlementAmount;
	}

	private static String requireRejectReason(String reason) {
		if (reason == null || reason.isBlank()) {
			throw new InvalidSettlementStateException("거절 사유는 필수입니다.");
		}
		if (reason.length() > REJECT_REASON_MAX_LENGTH) {
			throw new InvalidSettlementStateException("거절 사유는 500자를 초과할 수 없습니다.");
		}
		return reason;
	}
}
