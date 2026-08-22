package com.planwith.planwith_fo_membership.domain.model.vo;

import java.util.Objects;
import java.util.UUID;

public record SettlementUuid(UUID value) {

	public SettlementUuid {
		Objects.requireNonNull(value, "Settlement UUID is required.");
	}

	public static SettlementUuid from(String value) {
		return new SettlementUuid(UUID.fromString(value));
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
