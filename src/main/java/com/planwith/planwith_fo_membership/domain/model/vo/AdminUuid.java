package com.planwith.planwith_fo_membership.domain.model.vo;

import java.util.Objects;
import java.util.UUID;

public record AdminUuid(UUID value) {

	public AdminUuid {
		Objects.requireNonNull(value, "Admin UUID is required.");
	}

	public static AdminUuid from(String value) {
		return new AdminUuid(UUID.fromString(value));
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
