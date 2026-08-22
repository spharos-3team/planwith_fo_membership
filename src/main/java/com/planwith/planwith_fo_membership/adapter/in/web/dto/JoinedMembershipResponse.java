package com.planwith.planwith_fo_membership.adapter.in.web.dto;

import java.time.Instant;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가입한 멤버십")
public record JoinedMembershipResponse(
		@Schema(description = "구독 UUID")
		UUID subscriptionUuid,

		@Schema(description = "멤버십 UUID")
		UUID membershipUuid,

		@Schema(description = "크리에이터 UUID")
		UUID creatorUuid,

		@Schema(description = "멤버십 이름")
		String membershipName,

		@Schema(description = "월 구독 금액(TOKEN)")
		int monthlyPrice,

		@Schema(description = "가격 단위", example = "TOKEN")
		String priceUnit,

		@Schema(description = "구독 상태", example = "ACTIVE")
		String status,

		@Schema(description = "가입 시각")
		Instant startedAt
) {
}
