package com.planwith.planwith_fo_membership.adapter.in.web.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Creator 수익 조회 결과. 금액 단위는 원(KRW)이며 Creator 배분 수익만 포함한다.")
public record RevenueResponse(
		@Schema(description = "수익 계정 UUID")
		UUID revenueUuid,

		@Schema(description = "총 누적 수익(원)")
		long totalRevenue,

		@Schema(description = "정산 가능 수익(원)")
		long availableRevenue,

		@Schema(description = "정산 완료 수익(원)")
		long settledRevenue
) {
}
