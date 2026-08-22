package com.planwith.planwith_fo_membership.domain.event;

public final class MembershipEventTypes {

	public static final String AGGREGATE_SUBSCRIPTION = "Subscription";
	public static final String AGGREGATE_PAYMENT = "Payment";
	public static final String AGGREGATE_SETTLEMENT = "Settlement";

	public static final String MEMBERSHIP_SUBSCRIBED = "MembershipSubscribed";
	public static final String MEMBERSHIP_CANCELED = "MembershipCanceled";
	public static final String MEMBERSHIP_EXPIRED = "MembershipExpired";
	public static final String SETTLEMENT_REQUESTED = "SettlementRequested";
	public static final String SETTLEMENT_COMPLETED = "SettlementCompleted";

	public static final String TOKEN_DEDUCTION_REQUESTED = "TokenDeductionRequested";
	public static final String PAYMENT_COMPLETED = "PaymentCompleted";
	public static final String PAYMENT_FAILED = "PaymentFailed";
	public static final String PAYMENT_REFUNDED = "PaymentRefunded";

	private MembershipEventTypes() {
	}
}
