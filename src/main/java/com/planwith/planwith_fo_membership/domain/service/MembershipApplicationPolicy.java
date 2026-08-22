package com.planwith.planwith_fo_membership.domain.service;

import java.util.Set;

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
}
