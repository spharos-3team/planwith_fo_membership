package com.planwith.planwith_fo_membership.adapter.in.web.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "본인 멤버십 조회 결과")
public record MyMembershipResponse(
		@Schema(description = "멤버십 보유 여부")
		boolean hasMembership,

		@Schema(description = "멤버십 UUID")
		UUID membershipUuid,

		@Schema(description = "멤버십 이름")
		String membershipName,

		@Schema(description = "월 구독 금액(TOKEN)")
		Integer monthlyPrice,

		@Schema(description = "가격 단위", example = "TOKEN")
		String priceUnit,

		@Schema(description = "멤버십 상태", example = "PENDING")
		String status,

		@Schema(description = "거절 사유")
		String rejectReason
) {
}
