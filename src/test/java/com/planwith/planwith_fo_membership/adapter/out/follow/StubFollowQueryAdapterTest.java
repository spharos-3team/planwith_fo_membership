package com.planwith.planwith_fo_membership.adapter.out.follow;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_membership.domain.exception.UnsupportedMembershipOperationException;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;

class StubFollowQueryAdapterTest {

	@Test
	void isFollowingDoesNotAccessFollowDatabase() {
		StubFollowQueryAdapter adapter = new StubFollowQueryAdapter();

		assertThatThrownBy(() -> adapter.isFollowing(
				MemberUuid.from("11111111-1111-1111-1111-111111111111"),
				CreatorUuid.from("22222222-2222-2222-2222-222222222222")
		))
				.isInstanceOf(UnsupportedMembershipOperationException.class)
				.hasMessageContaining("Follow 서비스");
	}
}
