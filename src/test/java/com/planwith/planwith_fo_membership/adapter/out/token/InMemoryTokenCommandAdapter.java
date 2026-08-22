package com.planwith.planwith_fo_membership.adapter.out.token;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.planwith.planwith_fo_membership.application.command.TokenDeductionCommand;
import com.planwith.planwith_fo_membership.application.port.out.TokenCommandPort;
import com.planwith.planwith_fo_membership.application.query.TokenDeductionResult;

public class InMemoryTokenCommandAdapter implements TokenCommandPort {

	private final List<TokenDeductionCommand> requested = new ArrayList<>();

	@Override
	public TokenDeductionResult requestTokenDeduction(TokenDeductionCommand command) {
		requested.add(command);
		return new TokenDeductionResult(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
	}

	public List<TokenDeductionCommand> requested() {
		return List.copyOf(requested);
	}
}
