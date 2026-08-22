package com.planwith.planwith_fo_membership.adapter.in.web.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "멤버십 가입 자격 검증 결과")
public record ValidateJoinEligibilityResponse(
		@Schema(description = "가입 가능 여부")
		boolean eligible,

		@Schema(description = "팔로우 여부")
		boolean following,

		@Schema(description = "멤버십 UUID")
		UUID membershipUuid,

		@Schema(description = "크리에이터 UUID")
		UUID creatorUuid,

		@Schema(description = "월 구독 금액(TOKEN)")
		int monthlyPrice,

		@Schema(description = "가격 단위", example = "TOKEN")
		String priceUnit
) {
}
