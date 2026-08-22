package com.planwith.planwith_fo_membership.adapter.in.web;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.planwith.planwith_fo_membership.adapter.in.web.dto.EntitlementResponse;
import com.planwith.planwith_fo_membership.application.port.in.query.CheckContentAccessQueryUseCase;
import com.planwith.planwith_fo_membership.application.query.CheckContentAccessQuery;
import com.planwith.planwith_fo_membership.application.query.ContentAccessResult;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;

@RestController
@RequestMapping("/api/planwith-fo-membership")
public class MembershipEntitlementController {

	private static final Logger log = LoggerFactory.getLogger(MembershipEntitlementController.class);

	private final CheckContentAccessQueryUseCase checkContentAccessQueryUseCase;

	public MembershipEntitlementController(CheckContentAccessQueryUseCase checkContentAccessQueryUseCase) {
		this.checkContentAccessQueryUseCase = checkContentAccessQueryUseCase;
	}

	// 멤버십 접근 권한 조회
	@GetMapping("/memberships/me/entitlement/{creatorUuid}")
	public ResponseEntity<EntitlementResponse> getMyEntitlement(
			@RequestHeader("X-Member-UUID") UUID memberUuid,
			@PathVariable UUID creatorUuid
	) {
		log.info(
				"MembershipEntitlementController : GET getMyEntitlement : 멤버십 접근 권한 조회 요청 - memberUuid={}, creatorUuid={}",
				memberUuid,
				creatorUuid
		);
		ContentAccessResult result = checkContentAccessQueryUseCase.check(
				new CheckContentAccessQuery(new MemberUuid(memberUuid), new CreatorUuid(creatorUuid))
		);
		EntitlementResponse response = new EntitlementResponse(
				result.memberUuid().value(),
				result.creatorUuid().value(),
				result.allowed(),
				result.status()
		);
		log.info(
				"MembershipEntitlementController : GET getMyEntitlement : 멤버십 접근 권한 조회 완료 - memberUuid={}, creatorUuid={}, allowed={}",
				memberUuid,
				creatorUuid,
				response.allowed()
		);
		return ResponseEntity.ok(response);
	}
}
