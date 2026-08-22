package com.planwith.planwith_fo_membership.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "정산 신청 요청")
public record RequestSettlementRequest(
		@Schema(description = "정산 신청 금액(원)", example = "30000")
		@NotNull(message = "정산 금액은 필수입니다.")
		@Positive(message = "정산 금액은 0보다 커야 합니다.")
		Long settlementAmount
) {
}
