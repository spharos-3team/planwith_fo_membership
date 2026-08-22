package com.planwith.planwith_fo_membership.adapter.in.web;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.planwith.planwith_fo_membership.adapter.in.web.dto.StartTokenPaymentRequest;
import com.planwith.planwith_fo_membership.adapter.in.web.dto.StartTokenPaymentResponse;
import com.planwith.planwith_fo_membership.adapter.in.web.dto.ValidateJoinEligibilityRequest;
import com.planwith.planwith_fo_membership.adapter.in.web.dto.ValidateJoinEligibilityResponse;
import com.planwith.planwith_fo_membership.application.command.StartTokenPaymentCommand;
import com.planwith.planwith_fo_membership.application.command.ValidateJoinEligibilityCommand;
import com.planwith.planwith_fo_membership.application.port.in.command.StartTokenPaymentUseCase;
import com.planwith.planwith_fo_membership.application.port.in.command.ValidateJoinEligibilityUseCase;
import com.planwith.planwith_fo_membership.application.query.StartTokenPaymentResult;
import com.planwith.planwith_fo_membership.application.query.ValidateJoinEligibilityResult;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/planwith-fo-membership")
public class MembershipSubscriptionController {

	private static final Logger log = LoggerFactory.getLogger(MembershipSubscriptionController.class);

	private final ValidateJoinEligibilityUseCase validateJoinEligibilityUseCase;
	private final StartTokenPaymentUseCase startTokenPaymentUseCase;

	public MembershipSubscriptionController(
			ValidateJoinEligibilityUseCase validateJoinEligibilityUseCase,
			StartTokenPaymentUseCase startTokenPaymentUseCase
	) {
		this.validateJoinEligibilityUseCase = validateJoinEligibilityUseCase;
		this.startTokenPaymentUseCase = startTokenPaymentUseCase;
	}

	// 멤버십 가입 자격 검증
	@PostMapping("/memberships/subscriptions/validate")
	public ResponseEntity<ValidateJoinEligibilityResponse> validateJoinEligibility(
			@RequestHeader("X-Member-UUID") UUID memberUuid,
			@Valid @RequestBody ValidateJoinEligibilityRequest request
	) {
		log.info(
				"MembershipSubscriptionController : POST validateJoinEligibility : 가입 자격 검증 요청 - memberUuid={}, creatorUuid={}",
				memberUuid,
				request.creatorUuid()
		);
		ValidateJoinEligibilityResult result = validateJoinEligibilityUseCase.validate(
				new ValidateJoinEligibilityCommand(new MemberUuid(memberUuid), new CreatorUuid(request.creatorUuid()))
		);
		log.info(
				"MembershipSubscriptionController : POST validateJoinEligibility : 가입 자격 검증 완료 - memberUuid={}, membershipUuid={}",
				memberUuid,
				result.membershipUuid()
		);
		return ResponseEntity.ok(new ValidateJoinEligibilityResponse(
				result.eligible(),
				result.following(),
				result.membershipUuid().value(),
				result.creatorUuid().value(),
				result.monthlyPrice(),
				result.priceUnit()
		));
	}

	// 토큰 결제 시작
	@PostMapping("/memberships/subscriptions/payments")
	public ResponseEntity<StartTokenPaymentResponse> startTokenPayment(
			@RequestHeader("X-Member-UUID") UUID memberUuid,
			@Valid @RequestBody StartTokenPaymentRequest request
	) {
		log.info(
				"MembershipSubscriptionController : POST startTokenPayment : 토큰 결제 시작 요청 - memberUuid={}, creatorUuid={}",
				memberUuid,
				request.creatorUuid()
		);
		StartTokenPaymentResult result = startTokenPaymentUseCase.start(
				new StartTokenPaymentCommand(new MemberUuid(memberUuid), new CreatorUuid(request.creatorUuid()))
		);
		log.info(
				"MembershipSubscriptionController : POST startTokenPayment : 토큰 결제 시작 완료 - memberUuid={}, paymentUuid={}, status={}",
				memberUuid,
				result.paymentUuid(),
				result.status()
		);
		return ResponseEntity.status(HttpStatus.CREATED).body(new StartTokenPaymentResponse(
				result.paymentUuid().value(),
				result.subscriptionUuid().value(),
				result.amount(),
				result.priceUnit(),
				result.status().name()
		));
	}
}
