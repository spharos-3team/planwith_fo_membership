package com.planwith.planwith_fo_membership.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_membership.domain.model.SettlementStatus;

class SettlementPolicyTest {

	@Test
	void canRequestWhenAmountIsWithinAvailableRevenue() {
		assertThat(SettlementPolicy.canRequest(30_000L, 50_000L)).isTrue();
		assertThat(SettlementPolicy.canRequest(50_000L, 50_000L)).isTrue();
		assertThat(SettlementPolicy.canRequest(30_000L, 20_000L)).isFalse();
		assertThat(SettlementPolicy.canRequest(0L, 50_000L)).isFalse();
		assertThat(SettlementPolicy.canRequest(-1L, 50_000L)).isFalse();
	}

	@Test
	void adminProcessIsAllowedOnlyOnValidStatus() {
		assertThat(SettlementPolicy.canApprove(SettlementStatus.REQUESTED)).isTrue();
		assertThat(SettlementPolicy.canApprove(SettlementStatus.APPROVED)).isFalse();
		assertThat(SettlementPolicy.canReject(SettlementStatus.REQUESTED)).isTrue();
		assertThat(SettlementPolicy.canReject(SettlementStatus.PAID)).isFalse();
		assertThat(SettlementPolicy.canPay(SettlementStatus.APPROVED)).isTrue();
		assertThat(SettlementPolicy.canPay(SettlementStatus.REQUESTED)).isFalse();
	}
}
