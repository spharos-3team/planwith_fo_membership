package com.planwith.planwith_fo_membership.domain.model;

public enum MembershipSagaStatus {
	SUBSCRIBE_REQUESTED,
	PAYMENT_PENDING,
	PAYMENT_COMPLETED,
	ACTIVE,
	COMPENSATING,
	FAILED
}
