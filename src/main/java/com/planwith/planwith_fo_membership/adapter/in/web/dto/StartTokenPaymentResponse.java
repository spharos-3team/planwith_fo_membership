package com.planwith.planwith_fo_membership.adapter.in.web.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "토큰 결제 시작 결과")
public record StartTokenPaymentResponse(
		@Schema(description = "결제 UUID")
		UUID paymentUuid,

		@Schema(description = "구독 UUID")
		UUID subscriptionUuid,

		@Schema(description = "차감 요청 금액(TOKEN)")
		long amount,

		@Schema(description = "가격 단위", example = "TOKEN")
		String priceUnit,

		@Schema(description = "결제 상태", example = "READY")
		String status
) {
}
