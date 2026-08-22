package com.planwith.planwith_fo_membership.domain.exception;

public class SettlementAmountExceededException extends RuntimeException {

	public SettlementAmountExceededException(String message) {
		super(message);
	}
}
