package com.planwith.planwith_fo_membership.domain.model.vo;

import java.util.Objects;
import java.util.UUID;

public record SubscriptionUuid(UUID value) {

	public SubscriptionUuid {
		Objects.requireNonNull(value, "Subscription UUID is required.");
	}

	public static SubscriptionUuid from(String value) {
		return new SubscriptionUuid(UUID.fromString(value));
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
