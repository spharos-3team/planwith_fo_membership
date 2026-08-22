package com.planwith.planwith_fo_membership.domain.model.vo;

import java.util.Objects;
import java.util.UUID;

public record MembershipUuid(UUID value) {

	public MembershipUuid {
		Objects.requireNonNull(value, "Membership UUID is required.");
	}

	public static MembershipUuid from(String value) {
		return new MembershipUuid(UUID.fromString(value));
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
