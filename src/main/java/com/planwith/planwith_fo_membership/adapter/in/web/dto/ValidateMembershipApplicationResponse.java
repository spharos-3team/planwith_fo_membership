package com.planwith.planwith_fo_membership.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "멤버십 신청 검증 결과")
public record ValidateMembershipApplicationResponse(
		@Schema(description = "개설 가능 여부", example = "true")
		boolean eligible,

		@Schema(description = "현재 등급 코드", example = "EXPLORER")
		String gradeCode,

		@Schema(description = "월 구독 금액(TOKEN)", example = "12900")
		int monthlyPrice,

		@Schema(description = "가격 단위", example = "TOKEN")
		String priceUnit
) {
}
