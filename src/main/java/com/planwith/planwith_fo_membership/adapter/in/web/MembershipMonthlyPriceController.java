package com.planwith.planwith_fo_membership.adapter.in.web;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.planwith.planwith_fo_membership.adapter.in.web.dto.UpdateMembershipMonthlyPriceRequest;
import com.planwith.planwith_fo_membership.adapter.in.web.dto.UpdateMembershipMonthlyPriceResponse;
import com.planwith.planwith_fo_membership.application.command.UpdateMembershipCommand;
import com.planwith.planwith_fo_membership.application.port.in.command.UpdateMembershipUseCase;
import com.planwith.planwith_fo_membership.application.query.UpdateMembershipMonthlyPriceResult;
import com.planwith.planwith_fo_membership.config.OpenApiConfig;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/planwith-fo-membership")
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
@SecurityRequirement(name = OpenApiConfig.GATEWAY_USER_ID_SCHEME)
public class MembershipMonthlyPriceController {

	private static final Logger log = LoggerFactory.getLogger(MembershipMonthlyPriceController.class);

	private final UpdateMembershipUseCase updateMembershipUseCase;

	public MembershipMonthlyPriceController(UpdateMembershipUseCase updateMembershipUseCase) {
		this.updateMembershipUseCase = updateMembershipUseCase;
	}

	// 월 구독 토큰 설정
	@PatchMapping("/memberships/me/monthly-price")
	public ResponseEntity<UpdateMembershipMonthlyPriceResponse> updateMonthlyPrice(
			@RequestHeader("X-Auth-User-Id") UUID creatorUuid,
			@Valid @RequestBody UpdateMembershipMonthlyPriceRequest request
	) {
		log.info(
				"MembershipMonthlyPriceController : PATCH updateMonthlyPrice : 월 구독 토큰 설정 요청 - creatorUuid={}, monthlyPrice={}",
				creatorUuid,
				request.monthlyPrice()
		);
		UpdateMembershipMonthlyPriceResult result = updateMembershipUseCase.update(
				new UpdateMembershipCommand(new CreatorUuid(creatorUuid), request.monthlyPrice())
		);
		log.info(
				"MembershipMonthlyPriceController : PATCH updateMonthlyPrice : 월 구독 토큰 설정 완료 - creatorUuid={}, membershipUuid={}, monthlyPrice={}",
				creatorUuid,
				result.membershipUuid(),
				result.monthlyPrice()
		);
		return ResponseEntity.ok(new UpdateMembershipMonthlyPriceResponse(
				result.membershipUuid().value(),
				result.creatorUuid().value(),
				result.membershipName(),
				result.monthlyPrice(),
				result.priceUnit(),
				result.status().name()
		));
	}
}
