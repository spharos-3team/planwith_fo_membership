package com.planwith.planwith_fo_membership.application.command;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;

class TokenDeductionCommandTest {

	@Test
	void rejectsNonPositiveAmount() {
		assertThatThrownBy(() -> new TokenDeductionCommand(
				MemberUuid.from("11111111-1111-1111-1111-111111111111"),
				0L,
				"MEMBERSHIP_SUBSCRIPTION",
				"55555555-5555-5555-5555-555555555555",
				"멤버십 가입"
		)).isInstanceOf(IllegalArgumentException.class);
	}
}
