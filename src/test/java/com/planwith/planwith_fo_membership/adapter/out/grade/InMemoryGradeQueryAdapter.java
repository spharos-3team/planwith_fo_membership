package com.planwith.planwith_fo_membership.adapter.out.grade;

import java.util.HashMap;
import java.util.Map;

import com.planwith.planwith_fo_membership.application.port.out.GradeQueryPort;
import com.planwith.planwith_fo_membership.application.query.MemberGradeResult;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;

public class InMemoryGradeQueryAdapter implements GradeQueryPort {

	private final Map<MemberUuid, MemberGradeResult> grades = new HashMap<>();

	public void put(MemberGradeResult grade) {
		grades.put(grade.memberUuid(), grade);
	}

	public void clear() {
		grades.clear();
	}

	@Override
	public MemberGradeResult getMemberGrade(MemberUuid memberUuid) {
		MemberGradeResult grade = grades.get(memberUuid);
		if (grade == null) {
			throw new IllegalStateException("Grade is not prepared for member: " + memberUuid);
		}
		return grade;
	}
}
