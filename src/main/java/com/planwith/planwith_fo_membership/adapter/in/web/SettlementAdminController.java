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

import com.planwith.planwith_fo_membership.adapter.in.web.dto.ProcessSettlementResponse;
import com.planwith.planwith_fo_membership.adapter.in.web.dto.RejectSettlementRequest;
import com.planwith.planwith_fo_membership.application.command.ApproveSettlementCommand;
import com.planwith.planwith_fo_membership.application.command.PaySettlementCommand;
import com.planwith.planwith_fo_membership.application.command.RejectSettlementCommand;
import com.planwith.planwith_fo_membership.application.port.in.command.ApproveSettlementUseCase;
import com.planwith.planwith_fo_membership.application.port.in.command.PaySettlementUseCase;
import com.planwith.planwith_fo_membership.application.port.in.command.RejectSettlementUseCase;
import com.planwith.planwith_fo_membership.application.query.ProcessSettlementResult;
import com.planwith.planwith_fo_membership.domain.model.vo.AdminUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SettlementUuid;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/planwith-fo-membership/admin/settlements")
public class SettlementAdminController {

	private static final Logger log = LoggerFactory.getLogger(SettlementAdminController.class);

	private final ApproveSettlementUseCase approveSettlementUseCase;
	private final RejectSettlementUseCase rejectSettlementUseCase;
	private final PaySettlementUseCase paySettlementUseCase;

	public SettlementAdminController(
			ApproveSettlementUseCase approveSettlementUseCase,
			RejectSettlementUseCase rejectSettlementUseCase,
			PaySettlementUseCase paySettlementUseCase
	) {
		this.approveSettlementUseCase = approveSettlementUseCase;
		this.rejectSettlementUseCase = rejectSettlementUseCase;
		this.paySettlementUseCase = paySettlementUseCase;
	}

	// 정산 승인
	@PostMapping("/{settlementUuid}/approve")
	public ResponseEntity<ProcessSettlementResponse> approveSettlement(
			@RequestHeader("X-Auth-User-Id") UUID adminUuid,
			@PathVariable UUID settlementUuid
	) {
		log.info(
				"SettlementAdminController : POST approveSettlement : 정산 승인 요청 - settlementUuid={}, adminUuid={}",
				settlementUuid,
				adminUuid
		);
		ProcessSettlementResult result = approveSettlementUseCase.approve(
				new ApproveSettlementCommand(new SettlementUuid(settlementUuid), new AdminUuid(adminUuid), null)
		);
		log.info(
				"SettlementAdminController : POST approveSettlement : 정산 승인 완료 - settlementUuid={}, status={}",
				settlementUuid,
				result.status()
		);
		return ResponseEntity.ok(toResponse(result));
	}

	// 정산 거절
	@PostMapping("/{settlementUuid}/reject")
	public ResponseEntity<ProcessSettlementResponse> rejectSettlement(
			@RequestHeader("X-Auth-User-Id") UUID adminUuid,
			@PathVariable UUID settlementUuid,
			@Valid @RequestBody RejectSettlementRequest request
	) {
		log.info(
				"SettlementAdminController : POST rejectSettlement : 정산 거절 요청 - settlementUuid={}, adminUuid={}",
				settlementUuid,
				adminUuid
		);
		ProcessSettlementResult result = rejectSettlementUseCase.reject(
				new RejectSettlementCommand(
						new SettlementUuid(settlementUuid),
						new AdminUuid(adminUuid),
						request.rejectReason()
				)
		);
		log.info(
				"SettlementAdminController : POST rejectSettlement : 정산 거절 완료 - settlementUuid={}, status={}",
				settlementUuid,
				result.status()
		);
		return ResponseEntity.ok(toResponse(result));
	}

	// 정산 지급 완료
	@PostMapping("/{settlementUuid}/pay")
	public ResponseEntity<ProcessSettlementResponse> paySettlement(
			@RequestHeader("X-Auth-User-Id") UUID adminUuid,
			@PathVariable UUID settlementUuid
	) {
		log.info(
				"SettlementAdminController : POST paySettlement : 정산 지급 완료 요청 - settlementUuid={}, adminUuid={}",
				settlementUuid,
				adminUuid
		);
		ProcessSettlementResult result = paySettlementUseCase.pay(
				new PaySettlementCommand(new SettlementUuid(settlementUuid), new AdminUuid(adminUuid), null)
		);
		log.info(
				"SettlementAdminController : POST paySettlement : 정산 지급 완료 - settlementUuid={}, status={}",
				settlementUuid,
				result.status()
		);
		return ResponseEntity.ok(toResponse(result));
	}

	private static ProcessSettlementResponse toResponse(ProcessSettlementResult result) {
		return new ProcessSettlementResponse(
				result.settlementUuid().value(),
				result.creatorUuid().value(),
				result.adminUuid() == null ? null : result.adminUuid().value(),
				result.status().name(),
				result.settlementAmount(),
				result.availableRevenue(),
				result.reservedRevenue(),
				result.settledRevenue(),
				result.approvedAt(),
				result.paidAt(),
				result.rejectReason()
		);
	}
}
