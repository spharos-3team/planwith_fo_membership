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

import com.planwith.planwith_fo_membership.adapter.in.web.dto.RequestSettlementRequest;
import com.planwith.planwith_fo_membership.adapter.in.web.dto.RequestSettlementResponse;
import com.planwith.planwith_fo_membership.application.command.RequestSettlementCommand;
import com.planwith.planwith_fo_membership.application.port.in.command.RequestSettlementUseCase;
import com.planwith.planwith_fo_membership.application.query.RequestSettlementResult;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/planwith-fo-membership")
public class MembershipSettlementController {

	private static final Logger log = LoggerFactory.getLogger(MembershipSettlementController.class);

	private final RequestSettlementUseCase requestSettlementUseCase;

	public MembershipSettlementController(RequestSettlementUseCase requestSettlementUseCase) {
		this.requestSettlementUseCase = requestSettlementUseCase;
	}

	// 정산 신청
	@PostMapping("/memberships/me/settlements")
	public ResponseEntity<RequestSettlementResponse> requestSettlement(
			@RequestHeader("X-Auth-User-Id") UUID memberUuid,
			@Valid @RequestBody RequestSettlementRequest request
	) {
		log.info(
				"MembershipSettlementController : POST requestSettlement : 정산 신청 요청 - memberUuid={}, amount={}",
				memberUuid,
				request.settlementAmount()
		);
		RequestSettlementResult result = requestSettlementUseCase.request(
				new RequestSettlementCommand(new CreatorUuid(memberUuid), request.settlementAmount(), null)
		);
		log.info(
				"MembershipSettlementController : POST requestSettlement : 정산 신청 완료 - memberUuid={}, settlementUuid={}, status={}",
				memberUuid,
				result.settlementUuid(),
				result.status()
		);
		return ResponseEntity.status(HttpStatus.CREATED).body(new RequestSettlementResponse(
				result.settlementUuid().value(),
				result.status().name(),
				result.settlementAmount(),
				result.availableRevenue(),
				result.reservedRevenue(),
				result.requestedAt()
		));
	}
}
