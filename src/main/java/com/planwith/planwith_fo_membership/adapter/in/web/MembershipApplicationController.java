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

import com.planwith.planwith_fo_membership.adapter.in.web.dto.ApplyMembershipApplicationResponse;
import com.planwith.planwith_fo_membership.adapter.in.web.dto.ValidateMembershipApplicationRequest;
import com.planwith.planwith_fo_membership.adapter.in.web.dto.ValidateMembershipApplicationResponse;
import com.planwith.planwith_fo_membership.application.command.ValidateMembershipApplicationCommand;
import com.planwith.planwith_fo_membership.application.port.in.command.ApplyMembershipApplicationUseCase;
import com.planwith.planwith_fo_membership.application.port.in.command.ValidateMembershipApplicationUseCase;
import com.planwith.planwith_fo_membership.application.query.ApplyMembershipApplicationResult;
import com.planwith.planwith_fo_membership.application.query.ValidateMembershipApplicationResult;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/planwith-fo-membership")
public class MembershipApplicationController {

	private static final Logger log = LoggerFactory.getLogger(MembershipApplicationController.class);

	private final ValidateMembershipApplicationUseCase validateMembershipApplicationUseCase;
	private final ApplyMembershipApplicationUseCase applyMembershipApplicationUseCase;

	public MembershipApplicationController(
			ValidateMembershipApplicationUseCase validateMembershipApplicationUseCase,
			ApplyMembershipApplicationUseCase applyMembershipApplicationUseCase
	) {
		this.validateMembershipApplicationUseCase = validateMembershipApplicationUseCase;
		this.applyMembershipApplicationUseCase = applyMembershipApplicationUseCase;
	}

	// 멤버십 신청 검증
	@PostMapping("/memberships/applications/validate")
	public ResponseEntity<ValidateMembershipApplicationResponse> validateApplication(
			@RequestHeader("X-Auth-User-Id") UUID creatorUuid,
			@Valid @RequestBody ValidateMembershipApplicationRequest request
	) {
		log.info("MembershipApplicationController : POST validateApplication : 멤버십 신청 검증 요청 - creatorUuid={}", creatorUuid);
		ValidateMembershipApplicationResult result = validateMembershipApplicationUseCase.validate(
				toCommand(creatorUuid, request)
		);
		log.info(
				"MembershipApplicationController : POST validateApplication : 멤버십 신청 검증 완료 - creatorUuid={}, gradeCode={}",
				creatorUuid,
				result.gradeCode()
		);
		return ResponseEntity.ok(new ValidateMembershipApplicationResponse(
				result.eligible(),
				result.gradeCode(),
				result.monthlyPrice(),
				result.priceUnit()
		));
	}

	// 멤버십 신청
	@PostMapping("/memberships/applications")
	public ResponseEntity<ApplyMembershipApplicationResponse> applyApplication(
			@RequestHeader("X-Auth-User-Id") UUID creatorUuid,
			@Valid @RequestBody ValidateMembershipApplicationRequest request
	) {
		log.info("MembershipApplicationController : POST applyApplication : 멤버십 신청 요청 - creatorUuid={}", creatorUuid);
		ApplyMembershipApplicationResult result = applyMembershipApplicationUseCase.apply(
				toCommand(creatorUuid, request)
		);
		log.info(
				"MembershipApplicationController : POST applyApplication : 멤버십 신청 완료 - creatorUuid={}, membershipUuid={}",
				creatorUuid,
				result.membershipUuid()
		);
		return ResponseEntity.status(HttpStatus.CREATED).body(new ApplyMembershipApplicationResponse(
				result.membershipUuid().value(),
				result.creatorUuid().value(),
				result.membershipName(),
				result.monthlyPrice(),
				result.priceUnit(),
				result.status().name(),
				result.revenueUuid().value()
		));
	}

	private static ValidateMembershipApplicationCommand toCommand(
			UUID creatorUuid,
			ValidateMembershipApplicationRequest request
	) {
		return new ValidateMembershipApplicationCommand(
				new CreatorUuid(creatorUuid),
				request.membershipName(),
				request.description(),
				request.monthlyPrice(),
				request.priceUnit()
		);
	}
}
