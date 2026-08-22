package com.planwith.planwith_fo_membership.adapter.out.token;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_membership.application.command.TokenDeductionCommand;
import com.planwith.planwith_fo_membership.domain.exception.UnsupportedMembershipOperationException;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;

class StubTokenCommandAdapterTest {

	@Test
	void requestTokenDeductionDoesNotAccessTokenDatabase() {
		StubTokenCommandAdapter adapter = new StubTokenCommandAdapter();
		TokenDeductionCommand command = new TokenDeductionCommand(
				MemberUuid.from("11111111-1111-1111-1111-111111111111"),
				12900L,
				"MEMBERSHIP_SUBSCRIPTION",
				"55555555-5555-5555-5555-555555555555",
				"멤버십 가입"
		);

		assertThatThrownBy(() -> adapter.requestTokenDeduction(command))
				.isInstanceOf(UnsupportedMembershipOperationException.class)
				.hasMessageContaining("Token 서비스");
	}
}
