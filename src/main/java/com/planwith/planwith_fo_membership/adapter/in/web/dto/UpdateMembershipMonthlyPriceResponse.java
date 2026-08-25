package com.planwith.planwith_fo_membership.adapter.in.web.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "월 구독 토큰 설정 결과")
public record UpdateMembershipMonthlyPriceResponse(
		@Schema(description = "멤버십 UUID")
		UUID membershipUuid,

		@Schema(description = "크리에이터 UUID")
		UUID creatorUuid,

		@Schema(description = "멤버십 이름")
		String membershipName,

		@Schema(description = "월 구독 토큰")
		int monthlyPrice,

		@Schema(description = "가격 단위", example = "TOKEN")
		String priceUnit,

		@Schema(description = "멤버십 상태", example = "APPROVED")
		String status
) {
}
