package com.planwith.planwith_fo_membership.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "멤버십 거절 요청")
public record RejectMembershipRequest(
		@Schema(description = "거절 사유", example = "서류 미비")
		@NotBlank(message = "거절 사유는 필수입니다.")
		@Size(max = 100, message = "거절 사유는 100자를 초과할 수 없습니다.")
		String rejectReason
) {
}
