package com.planwith.planwith_fo_membership.adapter.out.grade;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_membership.domain.exception.UnsupportedMembershipOperationException;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;

class StubGradeQueryAdapterTest {

	@Test
	void getMemberGradeDoesNotAccessGradeDatabase() {
		StubGradeQueryAdapter adapter = new StubGradeQueryAdapter();

		assertThatThrownBy(() -> adapter.getMemberGrade(MemberUuid.from("11111111-1111-1111-1111-111111111111")))
				.isInstanceOf(UnsupportedMembershipOperationException.class)
				.hasMessageContaining("Grade 서비스");
	}
}
