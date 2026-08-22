package com.planwith.planwith_fo_membership.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_membership.application.port.in.query.GetMyMembershipQueryUseCase;
import com.planwith.planwith_fo_membership.application.port.in.query.GetRevenueQueryUseCase;
import com.planwith.planwith_fo_membership.application.port.in.query.ListCreatorSubscribersQueryUseCase;
import com.planwith.planwith_fo_membership.application.port.in.query.ListJoinedMembershipsQueryUseCase;
import com.planwith.planwith_fo_membership.application.port.out.SaveMembershipPort;
import com.planwith.planwith_fo_membership.application.port.out.SaveRevenuePort;
import com.planwith.planwith_fo_membership.application.port.out.SaveSubscriptionPort;
import com.planwith.planwith_fo_membership.application.query.CreatorSubscribersResult;
import com.planwith.planwith_fo_membership.application.query.GetMyMembershipQuery;
import com.planwith.planwith_fo_membership.application.query.GetRevenueQuery;
import com.planwith.planwith_fo_membership.application.query.JoinedMembershipResult;
import com.planwith.planwith_fo_membership.application.query.ListCreatorSubscribersQuery;
import com.planwith.planwith_fo_membership.application.query.ListJoinedMembershipsQuery;
import com.planwith.planwith_fo_membership.application.query.MyMembershipResult;
import com.planwith.planwith_fo_membership.application.query.RevenueResult;
import com.planwith.planwith_fo_membership.domain.model.Membership;
import com.planwith.planwith_fo_membership.domain.model.MembershipRevenue;
import com.planwith.planwith_fo_membership.domain.model.MembershipStatus;
import com.planwith.planwith_fo_membership.domain.model.MembershipSubscription;
import com.planwith.planwith_fo_membership.domain.model.SubscriptionStatus;
import com.planwith.planwith_fo_membership.domain.model.vo.AdminUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.RevenueUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;
import com.planwith.planwith_fo_membership.domain.service.MembershipApplicationPolicy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MembershipManagementQueryServiceIntegrationTest {

	private final CreatorUuid creatorUuid = CreatorUuid.from("22222222-2222-2222-2222-222222222222");
	private final MemberUuid subscriberUuid = MemberUuid.from("11111111-1111-1111-1111-111111111111");
	private final MembershipUuid membershipUuid = MembershipUuid.from("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
	private final SubscriptionUuid subscriptionUuid = SubscriptionUuid.from("55555555-5555-5555-5555-555555555555");
	private final RevenueUuid revenueUuid = RevenueUuid.from("66666666-6666-6666-6666-666666666666");

	@Autowired
	private GetMyMembershipQueryUseCase getMyMembershipQueryUseCase;

	@Autowired
	private ListJoinedMembershipsQueryUseCase listJoinedMembershipsQueryUseCase;

	@Autowired
	private ListCreatorSubscribersQueryUseCase listCreatorSubscribersQueryUseCase;

	@Autowired
	private GetRevenueQueryUseCase getRevenueQueryUseCase;

	@Autowired
	private SaveMembershipPort saveMembershipPort;

	@Autowired
	private SaveSubscriptionPort saveSubscriptionPort;

	@Autowired
	private SaveRevenuePort saveRevenuePort;

	@Test
	void queriesMyMembershipJoinedListSubscribersAndRevenue() {
		saveMembershipPort.save(Membership.apply(
				membershipUuid,
				creatorUuid,
				"윤휘명의 여행 멤버십",
				"월간 멤버십",
				100,
				Instant.parse("2026-08-22T00:00:00Z")
		).approve(AdminUuid.from("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")));
		saveSubscriptionPort.save(MembershipSubscription.active(
				subscriptionUuid,
				membershipUuid,
				subscriberUuid,
				Instant.parse("2026-08-22T00:01:00Z")
		));
		saveRevenuePort.save(MembershipRevenue.empty(revenueUuid, creatorUuid).record(12900L).settle(2900L));

		MyMembershipResult myMembership = getMyMembershipQueryUseCase.get(new GetMyMembershipQuery(creatorUuid))
				.orElseThrow();
		assertThat(myMembership.status()).isEqualTo(MembershipStatus.APPROVED);
		assertThat(myMembership.monthlyPrice()).isEqualTo(100);
		assertThat(myMembership.priceUnit()).isEqualTo(MembershipApplicationPolicy.PRICE_UNIT_TOKEN);

		JoinedMembershipResult joined = listJoinedMembershipsQueryUseCase
				.list(new ListJoinedMembershipsQuery(subscriberUuid))
				.get(0);
		assertThat(joined.membershipUuid()).isEqualTo(membershipUuid);
		assertThat(joined.creatorUuid()).isEqualTo(creatorUuid);
		assertThat(joined.membershipName()).isEqualTo("윤휘명의 여행 멤버십");
		assertThat(joined.status()).isEqualTo(SubscriptionStatus.ACTIVE);
		assertThat(joined.startedAt()).isEqualTo(Instant.parse("2026-08-22T00:01:00Z"));
		assertThat(joined.endedAt()).isNull();
		assertThat(joined.monthlyPrice()).isEqualTo(100);

		CreatorSubscribersResult subscribers = listCreatorSubscribersQueryUseCase.list(
				new ListCreatorSubscribersQuery(creatorUuid)
		);
		assertThat(subscribers.subscriberCount()).isEqualTo(1);
		assertThat(subscribers.subscribers().get(0).memberUuid()).isEqualTo(subscriberUuid);
		assertThat(subscribers.subscribers().get(0).status()).isEqualTo(SubscriptionStatus.ACTIVE);
		assertThat(subscribers.subscribers().get(0).startedAt()).isEqualTo(Instant.parse("2026-08-22T00:01:00Z"));

		RevenueResult revenue = getRevenueQueryUseCase.get(new GetRevenueQuery(creatorUuid));
		assertThat(revenue.totalRevenue()).isEqualTo(12900L);
		assertThat(revenue.availableRevenue()).isEqualTo(10000L);
		assertThat(revenue.settledRevenue()).isEqualTo(2900L);
	}

	@Test
	void returnsEmptyManagementViewsWhenCreatorHasNoData() {
		assertThat(getMyMembershipQueryUseCase.get(new GetMyMembershipQuery(creatorUuid))).isEmpty();
		assertThat(listJoinedMembershipsQueryUseCase.list(new ListJoinedMembershipsQuery(subscriberUuid))).isEmpty();

		CreatorSubscribersResult subscribers = listCreatorSubscribersQueryUseCase.list(
				new ListCreatorSubscribersQuery(creatorUuid)
		);
		assertThat(subscribers.subscriberCount()).isZero();

		RevenueResult revenue = getRevenueQueryUseCase.get(new GetRevenueQuery(creatorUuid));
		assertThat(revenue.revenueUuid()).isNull();
		assertThat(revenue.totalRevenue()).isZero();
		assertThat(revenue.availableRevenue()).isZero();
		assertThat(revenue.settledRevenue()).isZero();
	}
}
