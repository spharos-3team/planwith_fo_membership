package com.planwith.planwith_fo_membership.adapter.in.web.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "수익 조회 결과")
public record RevenueResponse(
		@Schema(description = "수익 계정 UUID")
		UUID revenueUuid,

		@Schema(description = "총 수익")
		long totalRevenue,

		@Schema(description = "정산 가능 금액")
		long availableRevenue,

		@Schema(description = "정산 완료 금액")
		long settledRevenue
) {
}
