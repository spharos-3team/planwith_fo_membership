package com.planwith.planwith_fo_membership.domain.service;

import com.planwith.planwith_fo_membership.domain.exception.InvalidSettlementStateException;
import com.planwith.planwith_fo_membership.domain.exception.SettlementAlreadyProcessedException;
import com.planwith.planwith_fo_membership.domain.exception.SettlementAmountExceededException;
import com.planwith.planwith_fo_membership.domain.model.SettlementStatus;

public final class SettlementPolicy {

	private SettlementPolicy() {
	}

	public static boolean canRequest(long requestAmount, long availableRevenue) {
		return requestAmount > 0 && availableRevenue >= requestAmount;
	}

	public static boolean canApprove(SettlementStatus status) {
		return status == SettlementStatus.REQUESTED;
	}

	public static boolean canReject(SettlementStatus status) {
		return status == SettlementStatus.REQUESTED;
	}

	public static boolean canPay(SettlementStatus status) {
		return status == SettlementStatus.APPROVED;
	}

	public static void requireCanRequest(long requestAmount, long availableRevenue) {
		if (requestAmount <= 0) {
			throw new InvalidSettlementStateException("정산 금액은 0보다 커야 합니다.");
		}
		if (availableRevenue < requestAmount) {
			throw new SettlementAmountExceededException("정산 가능 금액을 초과할 수 없습니다.");
		}
	}

	public static void requireCanApprove(SettlementStatus status) {
		if (!canApprove(status)) {
			throw new SettlementAlreadyProcessedException("신청된 정산만 승인할 수 있습니다.");
		}
	}

	public static void requireCanReject(SettlementStatus status) {
		if (!canReject(status)) {
			throw new SettlementAlreadyProcessedException("신청된 정산만 거절할 수 있습니다.");
		}
	}

	public static void requireCanPay(SettlementStatus status) {
		if (!canPay(status)) {
			throw new SettlementAlreadyProcessedException("승인된 정산만 지급 완료할 수 있습니다.");
		}
	}
}
