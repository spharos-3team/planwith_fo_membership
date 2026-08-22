package com.planwith.planwith_fo_membership.adapter.in.web.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "멤버십 접근 권한 조회 결과")
public record EntitlementResponse(
		@Schema(description = "회원 UUID")
		UUID memberUuid,

		@Schema(description = "크리에이터 UUID")
		UUID creatorUuid,

		@Schema(description = "접근 허용 여부")
		boolean allowed,

		@Schema(description = "권한 상태", example = "ACTIVE")
		String status
) {
}
