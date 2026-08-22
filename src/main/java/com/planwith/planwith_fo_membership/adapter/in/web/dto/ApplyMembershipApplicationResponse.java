package com.planwith.planwith_fo_membership.adapter.in.web.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "멤버십 신청 결과")
public record ApplyMembershipApplicationResponse(
		@Schema(description = "멤버십 UUID")
		UUID membershipUuid,

		@Schema(description = "크리에이터 UUID")
		UUID creatorUuid,

		@Schema(description = "멤버십 이름", example = "윤휘명의 여행 멤버십")
		String membershipName,

		@Schema(description = "월 구독 금액(TOKEN)", example = "100")
		int monthlyPrice,

		@Schema(description = "가격 단위", example = "TOKEN")
		String priceUnit,

		@Schema(description = "신청 상태", example = "PENDING")
		String status,

		@Schema(description = "수익 계정 UUID")
		UUID revenueUuid
) {
}
