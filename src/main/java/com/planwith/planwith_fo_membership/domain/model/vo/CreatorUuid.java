package com.planwith.planwith_fo_membership.domain.model.vo;

import java.util.Objects;
import java.util.UUID;

public record CreatorUuid(UUID value) {

	public CreatorUuid {
		Objects.requireNonNull(value, "Creator UUID is required.");
	}

	public static CreatorUuid from(String value) {
		return new CreatorUuid(UUID.fromString(value));
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
