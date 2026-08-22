package com.planwith.planwith_fo_membership.adapter.in.web.dto;

import java.time.Instant;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "정산 신청 결과")
public record RequestSettlementResponse(
		@Schema(description = "정산 UUID")
		UUID settlementUuid,

		@Schema(description = "정산 상태", example = "REQUESTED")
		String status,

		@Schema(description = "정산 신청 금액(원)")
		long settlementAmount,

		@Schema(description = "정산 가능 수익(원)")
		long availableRevenue,

		@Schema(description = "정산 신청 중 금액(원)")
		long reservedRevenue,

		@Schema(description = "신청 시각")
		Instant requestedAt
) {
}
