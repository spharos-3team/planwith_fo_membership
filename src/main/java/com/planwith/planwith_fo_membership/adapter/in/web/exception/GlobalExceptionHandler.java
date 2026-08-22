package com.planwith.planwith_fo_membership.adapter.in.web.exception;

import java.time.Instant;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.planwith.planwith_fo_membership.adapter.in.web.dto.ApiErrorResponse;
import com.planwith.planwith_fo_membership.domain.exception.InvalidCredentialsException;
import com.planwith.planwith_fo_membership.domain.exception.InvalidMembershipStateException;
import com.planwith.planwith_fo_membership.domain.exception.InvalidPaymentStateException;
import com.planwith.planwith_fo_membership.domain.exception.InvalidRevenueException;
import com.planwith.planwith_fo_membership.domain.exception.InvalidSettlementStateException;
import com.planwith.planwith_fo_membership.domain.exception.InvalidSubscriptionStateException;
import com.planwith.planwith_fo_membership.domain.exception.UnsupportedMembershipOperationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(InvalidCredentialsException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(InvalidCredentialsException exception) {
		return createErrorResponse(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", exception.getMessage());
	}

	@ExceptionHandler(InvalidMembershipStateException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidMembershipState(InvalidMembershipStateException exception) {
		return createErrorResponse(HttpStatus.CONFLICT, "INVALID_MEMBERSHIP_STATE", exception.getMessage());
	}

	@ExceptionHandler(InvalidSubscriptionStateException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidSubscriptionState(InvalidSubscriptionStateException exception) {
		return createErrorResponse(HttpStatus.CONFLICT, "INVALID_SUBSCRIPTION_STATE", exception.getMessage());
	}

	@ExceptionHandler(InvalidPaymentStateException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidPaymentState(InvalidPaymentStateException exception) {
		return createErrorResponse(HttpStatus.CONFLICT, "INVALID_PAYMENT_STATE", exception.getMessage());
	}

	@ExceptionHandler(InvalidRevenueException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidRevenue(InvalidRevenueException exception) {
		return createErrorResponse(HttpStatus.CONFLICT, "INVALID_REVENUE", exception.getMessage());
	}

	@ExceptionHandler(InvalidSettlementStateException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidSettlementState(InvalidSettlementStateException exception) {
		return createErrorResponse(HttpStatus.CONFLICT, "INVALID_SETTLEMENT_STATE", exception.getMessage());
	}

	@ExceptionHandler(UnsupportedMembershipOperationException.class)
	public ResponseEntity<ApiErrorResponse> handleUnsupportedOperation(UnsupportedMembershipOperationException exception) {
		return createErrorResponse(HttpStatus.NOT_IMPLEMENTED, "NOT_IMPLEMENTED", exception.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
		String message = exception.getBindingResult()
				.getFieldErrors()
				.stream()
				.findFirst()
				.map(DefaultMessageSourceResolvable::getDefaultMessage)
				.orElse("요청값이 올바르지 않습니다.");

		return createErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", message);
	}

	private ResponseEntity<ApiErrorResponse> createErrorResponse(
			HttpStatus status,
			String code,
			String message
	) {
		ApiErrorResponse response = new ApiErrorResponse(
				Instant.now(),
				status.value(),
				code,
				message
		);
		return ResponseEntity.status(status).body(response);
	}
}
