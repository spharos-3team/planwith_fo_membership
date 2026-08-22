package com.planwith.planwith_fo_membership.adapter.in.web.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Creator 가입자 조회 결과")
public record CreatorSubscribersResponse(
		@Schema(description = "현재 가입자 수")
		int subscriberCount,

		@Schema(description = "가입 회원 목록")
		List<SubscriberResponse> subscribers
) {

	@Schema(description = "가입 회원")
	public record SubscriberResponse(
			@Schema(description = "구독 UUID")
			UUID subscriptionUuid,

			@Schema(description = "회원 UUID")
			UUID memberUuid,

			@Schema(description = "구독 상태", example = "ACTIVE")
			String status,

			@Schema(description = "가입 시각")
			Instant startedAt,

			@Schema(description = "종료 시각")
			Instant endedAt
	) {
	}
}
