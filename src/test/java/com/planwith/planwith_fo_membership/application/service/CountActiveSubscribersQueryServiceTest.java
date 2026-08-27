package com.planwith.planwith_fo_membership.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_membership.application.port.out.CountActiveSubscribersPort;
import com.planwith.planwith_fo_membership.application.query.CreatorSubscriberCountResult;

class CountActiveSubscribersQueryServiceTest {

	private static final UUID CREATOR_A = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
	private static final UUID CREATOR_B = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

	@Test
	void returnsZeroWhenCreatorHasNoActiveSubscribers() {
		CountActiveSubscribersQueryService service = new CountActiveSubscribersQueryService(
				creatorUuids -> Map.of(CREATOR_A, 3L)
		);

		List<CreatorSubscriberCountResult> results = service.count(List.of(CREATOR_A, CREATOR_B));

		assertThat(results).containsExactly(
				new CreatorSubscriberCountResult(CREATOR_A, 3L),
				new CreatorSubscriberCountResult(CREATOR_B, 0L)
		);
	}

	@Test
	void returnsEmptyWhenCreatorUuidsMissing() {
		CountActiveSubscribersQueryService service = new CountActiveSubscribersQueryService(
				creatorUuids -> Map.of(CREATOR_A, 3L)
		);

		assertThat(service.count(List.of())).isEmpty();
		assertThat(service.count((Collection<UUID>) null)).isEmpty();
	}
}
