package com.planwith.planwith_fo_membership.adapter.in.web.dto;

import java.time.Instant;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 정산 처리 결과")
public record ProcessSettlementResponse(
		@Schema(description = "정산 UUID")
		UUID settlementUuid,

		@Schema(description = "크리에이터 UUID")
		UUID creatorUuid,

		@Schema(description = "처리 관리자 UUID")
		UUID adminUuid,

		@Schema(description = "정산 상태", example = "APPROVED")
		String status,

		@Schema(description = "정산 금액(원)")
		long settlementAmount,

		@Schema(description = "정산 가능 수익(원)")
		long availableRevenue,

		@Schema(description = "정산 신청 중 금액(원)")
		long reservedRevenue,

		@Schema(description = "정산 완료 수익(원)")
		long settledRevenue,

		@Schema(description = "승인 시각")
		Instant approvedAt,

		@Schema(description = "지급 완료 시각")
		Instant paidAt,

		@Schema(description = "거절 사유")
		String rejectReason
) {
}
