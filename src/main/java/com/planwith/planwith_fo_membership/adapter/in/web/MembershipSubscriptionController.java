package com.planwith.planwith_fo_membership.adapter.in.web;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.planwith.planwith_fo_membership.adapter.in.web.dto.CancelSubscriptionResponse;
import com.planwith.planwith_fo_membership.adapter.in.web.dto.StartTokenPaymentRequest;
import com.planwith.planwith_fo_membership.adapter.in.web.dto.StartTokenPaymentResponse;
import com.planwith.planwith_fo_membership.adapter.in.web.dto.ValidateJoinEligibilityRequest;
import com.planwith.planwith_fo_membership.adapter.in.web.dto.ValidateJoinEligibilityResponse;
import com.planwith.planwith_fo_membership.application.command.CancelSubscriptionCommand;
import com.planwith.planwith_fo_membership.application.command.StartTokenPaymentCommand;
import com.planwith.planwith_fo_membership.application.command.ValidateJoinEligibilityCommand;
import com.planwith.planwith_fo_membership.application.port.in.command.CancelSubscriptionUseCase;
import com.planwith.planwith_fo_membership.application.port.in.command.StartTokenPaymentUseCase;
import com.planwith.planwith_fo_membership.application.port.in.command.ValidateJoinEligibilityUseCase;
import com.planwith.planwith_fo_membership.application.query.CancelSubscriptionResult;
import com.planwith.planwith_fo_membership.application.query.StartTokenPaymentResult;
import com.planwith.planwith_fo_membership.application.query.ValidateJoinEligibilityResult;
import com.planwith.planwith_fo_membership.config.OpenApiConfig;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/planwith-fo-membership")
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
@SecurityRequirement(name = OpenApiConfig.GATEWAY_USER_ID_SCHEME)
public class MembershipSubscriptionController {

	private static final Logger log = LoggerFactory.getLogger(MembershipSubscriptionController.class);

	private final ValidateJoinEligibilityUseCase validateJoinEligibilityUseCase;
	private final StartTokenPaymentUseCase startTokenPaymentUseCase;
	private final CancelSubscriptionUseCase cancelSubscriptionUseCase;

	public MembershipSubscriptionController(
			ValidateJoinEligibilityUseCase validateJoinEligibilityUseCase,
			StartTokenPaymentUseCase startTokenPaymentUseCase,
			CancelSubscriptionUseCase cancelSubscriptionUseCase
	) {
		this.validateJoinEligibilityUseCase = validateJoinEligibilityUseCase;
		this.startTokenPaymentUseCase = startTokenPaymentUseCase;
		this.cancelSubscriptionUseCase = cancelSubscriptionUseCase;
	}

	// 멤버십 가입 자격 검증
	@PostMapping("/memberships/subscriptions/validate")
	public ResponseEntity<ValidateJoinEligibilityResponse> validateJoinEligibility(
			@RequestHeader("X-Auth-User-Id") UUID memberUuid,
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
			@RequestHeader("X-Auth-User-Id") UUID memberUuid,
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

	// 멤버십 해지
	@PostMapping("/memberships/me/subscriptions/{subscriptionUuid}/cancel")
	public ResponseEntity<CancelSubscriptionResponse> cancelSubscription(
			@RequestHeader("X-Auth-User-Id") UUID memberUuid,
			@PathVariable UUID subscriptionUuid
	) {
		log.info(
				"MembershipSubscriptionController : POST cancelSubscription : 멤버십 해지 요청 - memberUuid={}, subscriptionUuid={}",
				memberUuid,
				subscriptionUuid
		);
		CancelSubscriptionResult result = cancelSubscriptionUseCase.cancel(
				new CancelSubscriptionCommand(new MemberUuid(memberUuid), new SubscriptionUuid(subscriptionUuid), null)
		);
		log.info(
				"MembershipSubscriptionController : POST cancelSubscription : 멤버십 해지 완료 - memberUuid={}, subscriptionUuid={}, status={}",
				memberUuid,
				result.subscriptionUuid(),
				result.status()
		);
		return ResponseEntity.ok(new CancelSubscriptionResponse(
				result.subscriptionUuid().value(),
				result.status().name(),
				result.endedAt()
		));
	}
}
