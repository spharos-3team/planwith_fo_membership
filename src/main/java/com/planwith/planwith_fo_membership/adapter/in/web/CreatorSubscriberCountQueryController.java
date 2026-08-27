package com.planwith.planwith_fo_membership.adapter.in.web;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.planwith.planwith_fo_membership.adapter.in.web.dto.CreatorSubscriberCountsResponse;
import com.planwith.planwith_fo_membership.application.port.in.query.CountActiveSubscribersQueryUseCase;

@RestController
@RequestMapping("/api/planwith-fo-membership")
public class CreatorSubscriberCountQueryController {

	private static final Logger log = LoggerFactory.getLogger(CreatorSubscriberCountQueryController.class);

	private final CountActiveSubscribersQueryUseCase countActiveSubscribersQueryUseCase;

	public CreatorSubscriberCountQueryController(CountActiveSubscribersQueryUseCase countActiveSubscribersQueryUseCase) {
		this.countActiveSubscribersQueryUseCase = countActiveSubscribersQueryUseCase;
	}

	// Creator별 활성 구독자 수 조회
	@GetMapping("/memberships/creators/subscriber-counts")
	public ResponseEntity<CreatorSubscriberCountsResponse> countActiveSubscribers(
			@RequestParam(name = "creatorUuids", required = false) List<UUID> creatorUuids
	) {
		log.info(
				"CreatorSubscriberCountQueryController : GET countActiveSubscribers : 활성 구독자 수 조회 요청 - creatorCount={}",
				creatorUuids == null ? 0 : creatorUuids.size()
		);
		CreatorSubscriberCountsResponse response = new CreatorSubscriberCountsResponse(
				countActiveSubscribersQueryUseCase.count(creatorUuids == null ? List.of() : creatorUuids).stream()
						.map(item -> new CreatorSubscriberCountsResponse.Item(item.creatorUuid(), item.subscriberCount()))
						.toList()
		);
		log.info(
				"CreatorSubscriberCountQueryController : GET countActiveSubscribers : 활성 구독자 수 조회 완료 - creatorCount={}",
				response.items().size()
		);
		return ResponseEntity.ok(response);
	}
}
