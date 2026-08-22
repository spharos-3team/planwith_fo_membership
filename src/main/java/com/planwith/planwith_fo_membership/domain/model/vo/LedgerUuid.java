package com.planwith.planwith_fo_membership.domain.model.vo;

import java.util.Objects;
import java.util.UUID;

public record LedgerUuid(UUID value) {

	public LedgerUuid {
		Objects.requireNonNull(value, "Ledger UUID is required.");
	}

	public static LedgerUuid from(String value) {
		return new LedgerUuid(UUID.fromString(value));
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
