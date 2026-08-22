package com.planwith.planwith_fo_membership.domain.service;

import java.util.Set;

import com.planwith.planwith_fo_membership.domain.exception.InsufficientMembershipGradeException;
import com.planwith.planwith_fo_membership.domain.exception.InvalidMembershipPriceException;
import com.planwith.planwith_fo_membership.domain.model.Membership;
import com.planwith.planwith_fo_membership.domain.model.MembershipStatus;

public final class MembershipApplicationPolicy {

	public static final String PRICE_UNIT_TOKEN = "TOKEN";
	public static final String MIN_OPEN_GRADE_CODE = "EXPLORER";
	public static final int MIN_OPEN_GRADE_LEVEL = 4;

	private static final Set<String> OPENABLE_GRADE_CODES = Set.of(
			"EXPLORER",
			"ADVENTURE",
			"PLANWITH"
	);

	private MembershipApplicationPolicy() {
	}

	public static boolean canOpenMembership(String gradeCode, int gradeLevel) {
		if (gradeLevel >= MIN_OPEN_GRADE_LEVEL) {
			return true;
		}
		return gradeCode != null && OPENABLE_GRADE_CODES.contains(gradeCode);
	}

	public static boolean isTokenPriceUnit(String priceUnit) {
		return PRICE_UNIT_TOKEN.equalsIgnoreCase(priceUnit);
	}

	public static boolean isPositivePrice(int monthlyPrice) {
		return monthlyPrice > 0;
	}

	public static boolean isDuplicateApplication(Membership existing) {
		if (existing == null) {
			return false;
		}
		MembershipStatus status = existing.status();
		return status == MembershipStatus.PENDING || status == MembershipStatus.APPROVED;
	}

	public static void requireEligibleGrade(String gradeCode, int gradeLevel) {
		if (!canOpenMembership(gradeCode, gradeLevel)) {
			throw new InsufficientMembershipGradeException("Explorer 이상 등급만 멤버십을 개설할 수 있습니다.");
		}
	}

	public static void requireValidPrice(int monthlyPrice, String priceUnit) {
		if (!isPositivePrice(monthlyPrice)) {
			throw new InvalidMembershipPriceException("월 구독 금액은 0보다 커야 합니다.");
		}
		if (!isTokenPriceUnit(priceUnit)) {
			throw new InvalidMembershipPriceException("멤버십 가격 단위는 TOKEN 이어야 합니다.");
		}
	}
}
