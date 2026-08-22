package com.planwith.planwith_fo_membership.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_membership.adapter.out.persistence.subscription.InMemoryLoadSubscriptionPort;
import com.planwith.planwith_fo_membership.application.query.JoinedMembershipResult;
import com.planwith.planwith_fo_membership.application.query.ListJoinedMembershipsQuery;
import com.planwith.planwith_fo_membership.domain.model.Membership;
import com.planwith.planwith_fo_membership.domain.model.MembershipSubscription;
import com.planwith.planwith_fo_membership.domain.model.SubscriptionStatus;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;
import com.planwith.planwith_fo_membership.domain.service.MembershipApplicationPolicy;

class ListJoinedMembershipsQueryServiceTest {

	private final MemberUuid memberUuid = MemberUuid.from("11111111-1111-1111-1111-111111111111");
	private final CreatorUuid creatorUuid = CreatorUuid.from("22222222-2222-2222-2222-222222222222");
	private final MembershipUuid membershipUuid = MembershipUuid.from("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
	private final SubscriptionUuid subscriptionUuid = SubscriptionUuid.from("55555555-5555-5555-5555-555555555555");

	private InMemoryLoadSubscriptionPort subscriptionPort;
	private ListJoinedMembershipsQueryService service;

	@BeforeEach
	void setUp() {
		subscriptionPort = new InMemoryLoadSubscriptionPort();
		service = new ListJoinedMembershipsQueryService(subscriptionPort);
	}

	@Test
	void returnsEmptyWhenMemberHasNoActiveSubscription() {
		assertThat(service.list(new ListJoinedMembershipsQuery(memberUuid))).isEmpty();
	}

	@Test
	void returnsActiveJoinedMembershipsOnly() {
		Membership membership = Membership.apply(
				membershipUuid,
				creatorUuid,
				"윤휘명의 여행 멤버십",
				"월간 멤버십",
				100,
				Instant.parse("2026-08-22T00:00:00Z")
		);
		subscriptionPort.registerMembership(membership);
		subscriptionPort.save(MembershipSubscription.active(
				subscriptionUuid,
				membershipUuid,
				memberUuid,
				Instant.parse("2026-08-22T00:01:00Z")
		));
		subscriptionPort.save(MembershipSubscription.active(
				SubscriptionUuid.from("77777777-7777-7777-7777-777777777777"),
				MembershipUuid.from("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
				memberUuid,
				Instant.parse("2026-08-22T00:02:00Z")
		).deactivate(Instant.parse("2026-08-22T00:03:00Z")));

		List<JoinedMembershipResult> result = service.list(new ListJoinedMembershipsQuery(memberUuid));

		assertThat(result).hasSize(1);
		assertThat(result.get(0).subscriptionUuid()).isEqualTo(subscriptionUuid);
		assertThat(result.get(0).creatorUuid()).isEqualTo(creatorUuid);
		assertThat(result.get(0).membershipName()).isEqualTo("윤휘명의 여행 멤버십");
		assertThat(result.get(0).monthlyPrice()).isEqualTo(100);
		assertThat(result.get(0).priceUnit()).isEqualTo(MembershipApplicationPolicy.PRICE_UNIT_TOKEN);
		assertThat(result.get(0).status()).isEqualTo(SubscriptionStatus.ACTIVE);
	}
}
