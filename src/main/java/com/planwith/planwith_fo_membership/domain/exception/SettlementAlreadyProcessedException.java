package com.planwith.planwith_fo_membership.domain.exception;

public class SettlementAlreadyProcessedException extends RuntimeException {

	public SettlementAlreadyProcessedException(String message) {
		super(message);
	}
}
