package com.planwith.planwith_fo_membership.adapter.in.web.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "멤버십 승인/거절 결과")
public record ReviewMembershipResponse(
		@Schema(description = "멤버십 UUID")
		UUID membershipUuid,

		@Schema(description = "크리에이터 UUID")
		UUID creatorUuid,

		@Schema(description = "처리 관리자 UUID")
		UUID adminUuid,

		@Schema(description = "멤버십 상태", example = "APPROVED")
		String status,

		@Schema(description = "거절 사유", example = "서류 미비")
		String rejectReason
) {
}
