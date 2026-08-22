package com.planwith.planwith_fo_membership.domain.service;

public final class SettlementPolicy {

	private SettlementPolicy() {
	}

	public static boolean canRequest(long requestAmount, long availableRevenue) {
		return requestAmount > 0 && availableRevenue >= requestAmount;
	}
}
