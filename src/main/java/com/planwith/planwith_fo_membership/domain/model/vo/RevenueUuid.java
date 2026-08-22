package com.planwith.planwith_fo_membership.domain.model.vo;

import java.util.Objects;
import java.util.UUID;

public record RevenueUuid(UUID value) {

	public RevenueUuid {
		Objects.requireNonNull(value, "Revenue UUID is required.");
	}

	public static RevenueUuid from(String value) {
		return new RevenueUuid(UUID.fromString(value));
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
