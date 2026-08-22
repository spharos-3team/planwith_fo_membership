package com.planwith.planwith_fo_membership.adapter.in.web.exception;

import java.time.Instant;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.planwith.planwith_fo_membership.adapter.in.web.dto.ApiErrorResponse;
import com.planwith.planwith_fo_membership.domain.exception.DuplicateMembershipApplicationException;
import com.planwith.planwith_fo_membership.domain.exception.DuplicateSubscriptionException;
import com.planwith.planwith_fo_membership.domain.exception.FollowRequiredException;
import com.planwith.planwith_fo_membership.domain.exception.ForbiddenAdminException;
import com.planwith.planwith_fo_membership.domain.exception.ForbiddenCreatorException;
import com.planwith.planwith_fo_membership.domain.exception.InsufficientMembershipGradeException;
import com.planwith.planwith_fo_membership.domain.exception.InvalidCredentialsException;
import com.planwith.planwith_fo_membership.domain.exception.InvalidMembershipPriceException;
import com.planwith.planwith_fo_membership.domain.exception.InvalidMembershipStateException;
import com.planwith.planwith_fo_membership.domain.exception.InvalidPaymentStateException;
import com.planwith.planwith_fo_membership.domain.exception.InvalidRevenueException;
import com.planwith.planwith_fo_membership.domain.exception.InvalidSettlementStateException;
import com.planwith.planwith_fo_membership.domain.exception.InvalidSubscriptionStateException;
import com.planwith.planwith_fo_membership.domain.exception.MembershipErrorCodes;
import com.planwith.planwith_fo_membership.domain.exception.MembershipNotApprovedException;
import com.planwith.planwith_fo_membership.domain.exception.MembershipNotFoundException;
import com.planwith.planwith_fo_membership.domain.exception.SettlementAlreadyProcessedException;
import com.planwith.planwith_fo_membership.domain.exception.SettlementAmountExceededException;
import com.planwith.planwith_fo_membership.domain.exception.SettlementNotFoundException;
import com.planwith.planwith_fo_membership.domain.exception.TokenInsufficientException;
import com.planwith.planwith_fo_membership.domain.exception.TokenPaymentFailedException;
import com.planwith.planwith_fo_membership.domain.exception.UnsupportedMembershipOperationException;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(InvalidCredentialsException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(InvalidCredentialsException exception) {
		return createErrorResponse(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", exception.getMessage());
	}

	@ExceptionHandler(InsufficientMembershipGradeException.class)
	public ResponseEntity<ApiErrorResponse> handleInsufficientMembershipGrade(InsufficientMembershipGradeException exception) {
		return createErrorResponse(HttpStatus.FORBIDDEN, MembershipErrorCodes.MEMBERSHIP_GRADE_NOT_ELIGIBLE, exception.getMessage());
	}

	@ExceptionHandler(FollowRequiredException.class)
	public ResponseEntity<ApiErrorResponse> handleFollowRequired(FollowRequiredException exception) {
		return createErrorResponse(HttpStatus.FORBIDDEN, MembershipErrorCodes.FOLLOW_REQUIRED, exception.getMessage());
	}

	@ExceptionHandler(ForbiddenCreatorException.class)
	public ResponseEntity<ApiErrorResponse> handleForbiddenCreator(ForbiddenCreatorException exception) {
		return createErrorResponse(HttpStatus.FORBIDDEN, MembershipErrorCodes.FORBIDDEN_CREATOR, exception.getMessage());
	}

	@ExceptionHandler(ForbiddenAdminException.class)
	public ResponseEntity<ApiErrorResponse> handleForbiddenAdmin(ForbiddenAdminException exception) {
		return createErrorResponse(HttpStatus.FORBIDDEN, MembershipErrorCodes.FORBIDDEN_ADMIN, exception.getMessage());
	}

	@ExceptionHandler(DuplicateMembershipApplicationException.class)
	public ResponseEntity<ApiErrorResponse> handleDuplicateMembershipApplication(DuplicateMembershipApplicationException exception) {
		return createErrorResponse(HttpStatus.CONFLICT, "DUPLICATE_MEMBERSHIP_APPLICATION", exception.getMessage());
	}

	@ExceptionHandler(DuplicateSubscriptionException.class)
	public ResponseEntity<ApiErrorResponse> handleDuplicateSubscription(DuplicateSubscriptionException exception) {
		return createErrorResponse(HttpStatus.CONFLICT, MembershipErrorCodes.MEMBERSHIP_ALREADY_SUBSCRIBED, exception.getMessage());
	}

	@ExceptionHandler(InvalidMembershipPriceException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidMembershipPrice(InvalidMembershipPriceException exception) {
		return createErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_MEMBERSHIP_PRICE", exception.getMessage());
	}

	@ExceptionHandler(MembershipNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleMembershipNotFound(MembershipNotFoundException exception) {
		return createErrorResponse(HttpStatus.NOT_FOUND, MembershipErrorCodes.MEMBERSHIP_NOT_FOUND, exception.getMessage());
	}

	@ExceptionHandler(MembershipNotApprovedException.class)
	public ResponseEntity<ApiErrorResponse> handleMembershipNotApproved(MembershipNotApprovedException exception) {
		return createErrorResponse(HttpStatus.CONFLICT, MembershipErrorCodes.MEMBERSHIP_NOT_APPROVED, exception.getMessage());
	}

	@ExceptionHandler(SettlementNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleSettlementNotFound(SettlementNotFoundException exception) {
		return createErrorResponse(HttpStatus.NOT_FOUND, "SETTLEMENT_NOT_FOUND", exception.getMessage());
	}

	@ExceptionHandler(SettlementAmountExceededException.class)
	public ResponseEntity<ApiErrorResponse> handleSettlementAmountExceeded(SettlementAmountExceededException exception) {
		return createErrorResponse(HttpStatus.CONFLICT, MembershipErrorCodes.SETTLEMENT_AMOUNT_EXCEEDED, exception.getMessage());
	}

	@ExceptionHandler(SettlementAlreadyProcessedException.class)
	public ResponseEntity<ApiErrorResponse> handleSettlementAlreadyProcessed(SettlementAlreadyProcessedException exception) {
		return createErrorResponse(HttpStatus.CONFLICT, MembershipErrorCodes.SETTLEMENT_ALREADY_PROCESSED, exception.getMessage());
	}

	@ExceptionHandler(TokenInsufficientException.class)
	public ResponseEntity<ApiErrorResponse> handleTokenInsufficient(TokenInsufficientException exception) {
		return createErrorResponse(HttpStatus.CONFLICT, MembershipErrorCodes.TOKEN_INSUFFICIENT, exception.getMessage());
	}

	@ExceptionHandler(TokenPaymentFailedException.class)
	public ResponseEntity<ApiErrorResponse> handleTokenPaymentFailed(TokenPaymentFailedException exception) {
		return createErrorResponse(HttpStatus.CONFLICT, MembershipErrorCodes.TOKEN_PAYMENT_FAILED, exception.getMessage());
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

	@ExceptionHandler(MissingRequestHeaderException.class)
	public ResponseEntity<ApiErrorResponse> handleMissingRequestHeader(MissingRequestHeaderException exception) {
		if ("X-Admin-UUID".equals(exception.getHeaderName())) {
			return createErrorResponse(HttpStatus.FORBIDDEN, MembershipErrorCodes.FORBIDDEN_ADMIN, "관리자만 처리할 수 있습니다.");
		}
		if ("X-Member-UUID".equals(exception.getHeaderName())) {
			return createErrorResponse(HttpStatus.FORBIDDEN, MembershipErrorCodes.FORBIDDEN_CREATOR, "회원 인증 정보가 필요합니다.");
		}
		return createErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "필수 요청 헤더가 없습니다.");
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

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException exception) {
		String message = exception.getConstraintViolations()
				.stream()
				.findFirst()
				.map(violation -> violation.getMessage())
				.orElse("요청값이 올바르지 않습니다.");
		return createErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", message);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiErrorResponse> handleUnreadable(HttpMessageNotReadableException exception) {
		return createErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "요청값이 올바르지 않습니다.");
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException exception) {
		return createErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", exception.getMessage());
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
