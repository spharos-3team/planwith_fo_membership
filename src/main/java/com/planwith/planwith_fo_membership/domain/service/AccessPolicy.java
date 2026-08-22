package com.planwith.planwith_fo_membership.domain.service;

import com.planwith.planwith_fo_membership.domain.exception.ForbiddenAdminException;
import com.planwith.planwith_fo_membership.domain.exception.ForbiddenCreatorException;
import com.planwith.planwith_fo_membership.domain.model.vo.AdminUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;

public final class AccessPolicy {

	private AccessPolicy() {
	}

	public static boolean isSameCreator(CreatorUuid actor, CreatorUuid owner) {
		return actor != null && actor.equals(owner);
	}

	public static boolean isSameMember(MemberUuid actor, MemberUuid owner) {
		return actor != null && actor.equals(owner);
	}

	public static boolean isAdminPresent(AdminUuid adminUuid) {
		return adminUuid != null;
	}

	public static void requireCreator(CreatorUuid actor, CreatorUuid owner) {
		if (!isSameCreator(actor, owner)) {
			throw new ForbiddenCreatorException("해당 Creator 리소스에 접근할 수 없습니다.");
		}
	}

	public static void requireMember(MemberUuid actor, MemberUuid owner) {
		requireMember(actor, owner, "본인 리소스만 처리할 수 있습니다.");
	}

	public static void requireMember(MemberUuid actor, MemberUuid owner, String message) {
		if (!isSameMember(actor, owner)) {
			throw new ForbiddenCreatorException(message);
		}
	}

	public static void requireAdmin(AdminUuid adminUuid) {
		if (!isAdminPresent(adminUuid)) {
			throw new ForbiddenAdminException("관리자만 처리할 수 있습니다.");
		}
	}
}
