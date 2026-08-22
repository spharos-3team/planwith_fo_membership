package com.planwith.planwith_fo_membership.domain.exception;

public final class MembershipErrorCodes {

	public static final String MEMBERSHIP_GRADE_NOT_ELIGIBLE = "MEMBERSHIP_GRADE_NOT_ELIGIBLE";
	public static final String MEMBERSHIP_NOT_FOUND = "MEMBERSHIP_NOT_FOUND";
	public static final String MEMBERSHIP_NOT_APPROVED = "MEMBERSHIP_NOT_APPROVED";
	public static final String MEMBERSHIP_ALREADY_SUBSCRIBED = "MEMBERSHIP_ALREADY_SUBSCRIBED";

	public static final String FOLLOW_REQUIRED = "FOLLOW_REQUIRED";

	public static final String TOKEN_INSUFFICIENT = "TOKEN_INSUFFICIENT";
	public static final String TOKEN_PAYMENT_FAILED = "TOKEN_PAYMENT_FAILED";

	public static final String SETTLEMENT_AMOUNT_EXCEEDED = "SETTLEMENT_AMOUNT_EXCEEDED";
	public static final String SETTLEMENT_ALREADY_PROCESSED = "SETTLEMENT_ALREADY_PROCESSED";

	public static final String FORBIDDEN_CREATOR = "FORBIDDEN_CREATOR";
	public static final String FORBIDDEN_ADMIN = "FORBIDDEN_ADMIN";

	private MembershipErrorCodes() {
	}
}
