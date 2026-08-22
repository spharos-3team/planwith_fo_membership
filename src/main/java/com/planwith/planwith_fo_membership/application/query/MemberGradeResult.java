package com.planwith.planwith_fo_membership.application.query;

import java.util.Objects;

import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;

public record MemberGradeResult(
		MemberUuid memberUuid,
		String gradeCode,
		String gradeName,
		int gradeLevel
) {

	public MemberGradeResult {
		Objects.requireNonNull(memberUuid, "Member UUID is required.");
		if (gradeCode == null || gradeCode.isBlank()) {
			throw new IllegalArgumentException("Grade code is required.");
		}
		if (gradeName == null || gradeName.isBlank()) {
			throw new IllegalArgumentException("Grade name is required.");
		}
	}
}
