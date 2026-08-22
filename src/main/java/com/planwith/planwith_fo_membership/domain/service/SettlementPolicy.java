package com.planwith.planwith_fo_membership.domain.service;

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
}
