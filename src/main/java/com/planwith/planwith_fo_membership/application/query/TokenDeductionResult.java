package com.planwith.planwith_fo_membership.application.query;

import java.util.Objects;
import java.util.UUID;

public record TokenDeductionResult(UUID transactionUuid) {

	public TokenDeductionResult {
		Objects.requireNonNull(transactionUuid, "Transaction UUID is required.");
	}
}
