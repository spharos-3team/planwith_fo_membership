package com.planwith.planwith_fo_membership.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

@Schema(description = "월 구독 토큰 설정 요청")
public record UpdateMembershipMonthlyPriceRequest(
		@Schema(description = "월 구독 토큰", example = "40")
		@Min(value = 10, message = "월 구독 토큰은 최소 10토큰부터 설정 가능합니다.")
		int monthlyPrice
) {
}
