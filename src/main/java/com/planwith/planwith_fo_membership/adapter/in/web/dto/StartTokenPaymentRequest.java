package com.planwith.planwith_fo_membership.adapter.in.web.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "토큰 결제 시작 요청")
public record StartTokenPaymentRequest(
		@Schema(description = "크리에이터 UUID", example = "22222222-2222-2222-2222-222222222222")
		@NotNull(message = "크리에이터 UUID는 필수입니다.")
		UUID creatorUuid
) {
}
