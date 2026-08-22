package com.planwith.planwith_fo_membership.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "멤버십 신청 검증 요청")
public record ValidateMembershipApplicationRequest(
		@Schema(description = "멤버십 이름", example = "크리에이터 멤버십")
		@NotBlank(message = "멤버십 이름은 필수입니다.")
		@Size(max = 100, message = "멤버십 이름은 100자를 초과할 수 없습니다.")
		String membershipName,

		@Schema(description = "멤버십 설명", example = "월간 멤버십")
		@Size(max = 1000, message = "멤버십 설명은 1000자를 초과할 수 없습니다.")
		String description,

		@Schema(description = "월 구독 금액(TOKEN)", example = "12900")
		@Positive(message = "월 구독 금액은 0보다 커야 합니다.")
		int monthlyPrice,

		@Schema(description = "가격 단위", example = "TOKEN")
		@NotBlank(message = "가격 단위는 필수입니다.")
		@Pattern(regexp = "TOKEN", message = "멤버십 가격 단위는 TOKEN 이어야 합니다.")
		String priceUnit
) {
}
