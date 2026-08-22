package com.planwith.planwith_fo_membership.domain.model.vo;

import java.util.Objects;
import java.util.UUID;

public record PaymentUuid(UUID value) {

	public PaymentUuid {
		Objects.requireNonNull(value, "Payment UUID is required.");
	}

	public static PaymentUuid from(String value) {
		return new PaymentUuid(UUID.fromString(value));
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
