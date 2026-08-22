package com.planwith.planwith_fo_membership.application.port.out;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_membership.adapter.out.follow.InMemoryFollowQueryAdapter;
import com.planwith.planwith_fo_membership.adapter.out.grade.InMemoryGradeQueryAdapter;
import com.planwith.planwith_fo_membership.adapter.out.token.InMemoryTokenCommandAdapter;
import com.planwith.planwith_fo_membership.application.command.TokenDeductionCommand;
import com.planwith.planwith_fo_membership.application.query.MemberGradeResult;
import com.planwith.planwith_fo_membership.application.query.TokenDeductionResult;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;

class ExternalDomainPortContractTest {

	private final MemberUuid memberUuid = MemberUuid.from("11111111-1111-1111-1111-111111111111");
	private final CreatorUuid creatorUuid = CreatorUuid.from("22222222-2222-2222-2222-222222222222");

	@Test
	void gradeQueryPortReturnsMemberGradeWithoutGradeDatabase() {
		InMemoryGradeQueryAdapter gradeQueryPort = new InMemoryGradeQueryAdapter();
		gradeQueryPort.put(new MemberGradeResult(memberUuid, "EXPLORER", "탐험가", 4));

		MemberGradeResult grade = gradeQueryPort.getMemberGrade(memberUuid);

		assertThat(grade.gradeCode()).isEqualTo("EXPLORER");
		assertThat(grade.gradeName()).isEqualTo("탐험가");
		assertThat(grade.gradeLevel()).isEqualTo(4);
	}

	@Test
	void followQueryPortChecksFollowingWithoutFollowDatabase() {
		InMemoryFollowQueryAdapter followQueryPort = new InMemoryFollowQueryAdapter();
		followQueryPort.follow(memberUuid, creatorUuid);

		assertThat(followQueryPort.isFollowing(memberUuid, creatorUuid)).isTrue();
		assertThat(followQueryPort.isFollowing(
				memberUuid,
				CreatorUuid.from("33333333-3333-3333-3333-333333333333")
		)).isFalse();
	}

	@Test
	void tokenCommandPortRequestsDeductionWithoutTokenDatabase() {
		InMemoryTokenCommandAdapter tokenCommandPort = new InMemoryTokenCommandAdapter();
		TokenDeductionCommand command = new TokenDeductionCommand(
				memberUuid,
				12900L,
				"MEMBERSHIP_SUBSCRIPTION",
				"55555555-5555-5555-5555-555555555555",
				"멤버십 가입"
		);

		TokenDeductionResult result = tokenCommandPort.requestTokenDeduction(command);

		assertThat(result.transactionUuid()).isNotNull();
		assertThat(tokenCommandPort.requested()).containsExactly(command);
	}
}
