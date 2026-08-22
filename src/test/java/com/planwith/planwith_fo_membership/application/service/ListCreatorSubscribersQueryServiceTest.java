package com.planwith.planwith_fo_membership.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_membership.adapter.out.persistence.subscription.InMemoryLoadSubscriptionPort;
import com.planwith.planwith_fo_membership.application.query.CreatorSubscribersResult;
import com.planwith.planwith_fo_membership.application.query.ListCreatorSubscribersQuery;
import com.planwith.planwith_fo_membership.domain.model.Membership;
import com.planwith.planwith_fo_membership.domain.model.MembershipSubscription;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;

class ListCreatorSubscribersQueryServiceTest {

	private final CreatorUuid creatorUuid = CreatorUuid.from("22222222-2222-2222-2222-222222222222");
	private final MemberUuid memberUuid = MemberUuid.from("11111111-1111-1111-1111-111111111111");
	private final MembershipUuid membershipUuid = MembershipUuid.from("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
	private final SubscriptionUuid subscriptionUuid = SubscriptionUuid.from("55555555-5555-5555-5555-555555555555");

	private InMemoryLoadSubscriptionPort subscriptionPort;
	private ListCreatorSubscribersQueryService service;

	@BeforeEach
	void setUp() {
		subscriptionPort = new InMemoryLoadSubscriptionPort();
		service = new ListCreatorSubscribersQueryService(subscriptionPort);
	}

	@Test
	void returnsEmptyWhenCreatorHasNoSubscribers() {
		CreatorSubscribersResult result = service.list(new ListCreatorSubscribersQuery(creatorUuid));

		assertThat(result.subscriberCount()).isZero();
		assertThat(result.subscribers()).isEmpty();
	}

	@Test
	void returnsActiveSubscriberCountAndList() {
		subscriptionPort.registerMembership(Membership.apply(
				membershipUuid,
				creatorUuid,
				"윤휘명의 여행 멤버십",
				"월간 멤버십",
				100,
				Instant.parse("2026-08-22T00:00:00Z")
		));
		subscriptionPort.save(MembershipSubscription.active(
				subscriptionUuid,
				membershipUuid,
				memberUuid,
				Instant.parse("2026-08-22T00:01:00Z")
		));

		CreatorSubscribersResult result = service.list(new ListCreatorSubscribersQuery(creatorUuid));

		assertThat(result.subscriberCount()).isEqualTo(1);
		assertThat(result.subscribers()).hasSize(1);
		assertThat(result.subscribers().get(0).subscriptionUuid()).isEqualTo(subscriptionUuid);
		assertThat(result.subscribers().get(0).memberUuid()).isEqualTo(memberUuid);
	}
}
