package com.planwith.planwith_fo_membership.adapter.in.web.dto;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Creator별 활성 구독자 수")
public record CreatorSubscriberCountsResponse(
		@Schema(description = "Creator별 활성 구독자 수 목록")
		List<Item> items
) {

	@Schema(description = "Creator 활성 구독자 수")
	public record Item(
			@Schema(description = "크리에이터 UUID")
			UUID creatorUuid,

			@Schema(description = "활성 구독자 수")
			long subscriberCount
	) {
	}
}
