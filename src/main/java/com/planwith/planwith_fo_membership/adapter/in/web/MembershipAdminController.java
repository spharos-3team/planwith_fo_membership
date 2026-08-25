package com.planwith.planwith_fo_membership.adapter.in.web;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.planwith.planwith_fo_membership.adapter.in.web.dto.RejectMembershipRequest;
import com.planwith.planwith_fo_membership.adapter.in.web.dto.ReviewMembershipResponse;
import com.planwith.planwith_fo_membership.application.command.ApproveMembershipCommand;
import com.planwith.planwith_fo_membership.application.command.RejectMembershipCommand;
import com.planwith.planwith_fo_membership.application.port.in.command.ApproveMembershipUseCase;
import com.planwith.planwith_fo_membership.application.port.in.command.RejectMembershipUseCase;
import com.planwith.planwith_fo_membership.application.query.ReviewMembershipResult;
import com.planwith.planwith_fo_membership.domain.model.vo.AdminUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/planwith-fo-membership/admin/memberships")
public class MembershipAdminController {

	private static final Logger log = LoggerFactory.getLogger(MembershipAdminController.class);

	private final ApproveMembershipUseCase approveMembershipUseCase;
	private final RejectMembershipUseCase rejectMembershipUseCase;

	public MembershipAdminController(
			ApproveMembershipUseCase approveMembershipUseCase,
			RejectMembershipUseCase rejectMembershipUseCase
	) {
		this.approveMembershipUseCase = approveMembershipUseCase;
		this.rejectMembershipUseCase = rejectMembershipUseCase;
	}

	// 멤버십 승인
	@PostMapping("/{membershipUuid}/approve")
	public ResponseEntity<ReviewMembershipResponse> approveMembership(
			@RequestHeader("X-Auth-User-Id") UUID adminUuid,
			@PathVariable UUID membershipUuid
	) {
		log.info(
				"MembershipAdminController : POST approveMembership : 멤버십 승인 요청 - membershipUuid={}, adminUuid={}",
				membershipUuid,
				adminUuid
		);
		ReviewMembershipResult result = approveMembershipUseCase.approve(
				new ApproveMembershipCommand(new MembershipUuid(membershipUuid), new AdminUuid(adminUuid))
		);
		log.info(
				"MembershipAdminController : POST approveMembership : 멤버십 승인 완료 - membershipUuid={}, status={}",
				membershipUuid,
				result.status()
		);
		return ResponseEntity.ok(toResponse(result));
	}

	// 멤버십 거절
	@PostMapping("/{membershipUuid}/reject")
	public ResponseEntity<ReviewMembershipResponse> rejectMembership(
			@RequestHeader("X-Auth-User-Id") UUID adminUuid,
			@PathVariable UUID membershipUuid,
			@Valid @RequestBody RejectMembershipRequest request
	) {
		log.info(
				"MembershipAdminController : POST rejectMembership : 멤버십 거절 요청 - membershipUuid={}, adminUuid={}",
				membershipUuid,
				adminUuid
		);
		ReviewMembershipResult result = rejectMembershipUseCase.reject(
				new RejectMembershipCommand(
						new MembershipUuid(membershipUuid),
						new AdminUuid(adminUuid),
						request.rejectReason()
				)
		);
		log.info(
				"MembershipAdminController : POST rejectMembership : 멤버십 거절 완료 - membershipUuid={}, status={}",
				membershipUuid,
				result.status()
		);
		return ResponseEntity.ok(toResponse(result));
	}

	private static ReviewMembershipResponse toResponse(ReviewMembershipResult result) {
		return new ReviewMembershipResponse(
				result.membershipUuid().value(),
				result.creatorUuid().value(),
				result.adminUuid().value(),
				result.status().name(),
				result.rejectReason()
		);
	}
}
