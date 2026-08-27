package com.planwith.planwith_fo_membership.application.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_membership.application.port.in.query.CountActiveSubscribersQueryUseCase;
import com.planwith.planwith_fo_membership.application.port.out.CountActiveSubscribersPort;
import com.planwith.planwith_fo_membership.application.query.CreatorSubscriberCountResult;

@Service
public class CountActiveSubscribersQueryService implements CountActiveSubscribersQueryUseCase {

	private static final Logger log = LoggerFactory.getLogger(CountActiveSubscribersQueryService.class);

	private final CountActiveSubscribersPort countActiveSubscribersPort;

	public CountActiveSubscribersQueryService(CountActiveSubscribersPort countActiveSubscribersPort) {
		this.countActiveSubscribersPort = countActiveSubscribersPort;
	}

	@Override
	@Transactional(readOnly = true)
	public List<CreatorSubscriberCountResult> count(Collection<UUID> creatorUuids) {
		log.info(
				"CountActiveSubscribersQueryService : count : 활성 구독자 수 조회 시작 - creatorCount={}",
				creatorUuids == null ? 0 : creatorUuids.size()
		);
		if (creatorUuids == null || creatorUuids.isEmpty()) {
			return List.of();
		}
		Map<UUID, Long> counts = countActiveSubscribersPort.countActiveByCreatorUuids(creatorUuids);
		List<CreatorSubscriberCountResult> results = creatorUuids.stream()
				.distinct()
				.map(creatorUuid -> new CreatorSubscriberCountResult(
						creatorUuid,
						counts.getOrDefault(creatorUuid, 0L)
				))
				.toList();
		log.info(
				"CountActiveSubscribersQueryService : count : 활성 구독자 수 조회 완료 - creatorCount={}",
				results.size()
		);
		return results;
	}
}
