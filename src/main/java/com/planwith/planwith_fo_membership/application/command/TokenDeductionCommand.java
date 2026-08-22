package com.planwith.planwith_fo_membership.application.command;

import java.util.Objects;

import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;

public record TokenDeductionCommand(
		MemberUuid memberUuid,
		long amount,
		String referenceType,
		String referenceUuid,
		String description
) {

	public TokenDeductionCommand {
		Objects.requireNonNull(memberUuid, "Member UUID is required.");
		if (amount <= 0) {
			throw new IllegalArgumentException("Token deduction amount must be greater than 0.");
		}
		if (referenceType == null || referenceType.isBlank()) {
			throw new IllegalArgumentException("Reference type is required.");
		}
		if (referenceUuid == null || referenceUuid.isBlank()) {
			throw new IllegalArgumentException("Reference UUID is required.");
		}
	}
}
