package com.planwith.planwith_fo_membership.domain.exception;

public class ForbiddenAdminException extends RuntimeException {

	public ForbiddenAdminException(String message) {
		super(message);
	}
}
