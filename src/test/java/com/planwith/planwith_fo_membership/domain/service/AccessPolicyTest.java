package com.planwith.planwith_fo_membership.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_membership.domain.exception.ForbiddenAdminException;
import com.planwith.planwith_fo_membership.domain.exception.ForbiddenCreatorException;
import com.planwith.planwith_fo_membership.domain.model.vo.AdminUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;

class AccessPolicyTest {

	private final CreatorUuid creator = CreatorUuid.from("22222222-2222-2222-2222-222222222222");
	private final MemberUuid member = MemberUuid.from("11111111-1111-1111-1111-111111111111");
	private final AdminUuid admin = AdminUuid.from("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

	@Test
	void requireCreatorRejectsOtherCreator() {
		AccessPolicy.requireCreator(creator, creator);

		assertThatThrownBy(() -> AccessPolicy.requireCreator(
				CreatorUuid.from("33333333-3333-3333-3333-333333333333"),
				creator
		)).isInstanceOf(ForbiddenCreatorException.class);
	}

	@Test
	void requireMemberRejectsOtherMember() {
		AccessPolicy.requireMember(member, member, "본인 구독만 해지할 수 있습니다.");

		assertThatThrownBy(() -> AccessPolicy.requireMember(
				MemberUuid.from("33333333-3333-3333-3333-333333333333"),
				member,
				"본인 구독만 해지할 수 있습니다."
		)).isInstanceOf(ForbiddenCreatorException.class)
				.hasMessage("본인 구독만 해지할 수 있습니다.");
	}

	@Test
	void requireAdminRejectsMissingAdmin() {
		AccessPolicy.requireAdmin(admin);
		assertThat(AccessPolicy.isAdminPresent(null)).isFalse();
		assertThatThrownBy(() -> AccessPolicy.requireAdmin(null))
				.isInstanceOf(ForbiddenAdminException.class);
	}
}
