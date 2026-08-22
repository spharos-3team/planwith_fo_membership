package com.planwith.planwith_fo_membership.adapter.in.web.dto;

import java.time.Instant;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "멤버십 해지 결과")
public record CancelSubscriptionResponse(
		@Schema(description = "구독 UUID")
		UUID subscriptionUuid,

		@Schema(description = "구독 상태", example = "INACTIVE")
		String status,

		@Schema(description = "해지 시각")
		Instant endedAt
) {
}
