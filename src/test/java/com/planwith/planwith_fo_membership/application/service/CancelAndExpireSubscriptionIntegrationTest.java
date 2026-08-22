package com.planwith.planwith_fo_membership.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_membership.adapter.out.redis.InMemoryEntitlementCacheAdapter;
import com.planwith.planwith_fo_membership.application.command.CancelSubscriptionCommand;
import com.planwith.planwith_fo_membership.application.port.in.command.CancelSubscriptionUseCase;
import com.planwith.planwith_fo_membership.application.port.in.command.ExpireDueSubscriptionsUseCase;
import com.planwith.planwith_fo_membership.application.port.in.query.CheckContentAccessQueryUseCase;
import com.planwith.planwith_fo_membership.application.port.out.LoadSubscriptionPort;
import com.planwith.planwith_fo_membership.application.port.out.SaveMembershipPort;
import com.planwith.planwith_fo_membership.application.port.out.SaveSubscriptionPort;
import com.planwith.planwith_fo_membership.application.query.CheckContentAccessQuery;
import com.planwith.planwith_fo_membership.application.query.ContentAccessResult;
import com.planwith.planwith_fo_membership.domain.event.MembershipEventTypes;
import com.planwith.planwith_fo_membership.domain.model.Entitlement;
import com.planwith.planwith_fo_membership.domain.model.Membership;
import com.planwith.planwith_fo_membership.domain.model.MembershipSubscription;
import com.planwith.planwith_fo_membership.domain.model.SubscriptionStatus;
import com.planwith.planwith_fo_membership.domain.model.vo.AdminUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CancelAndExpireSubscriptionIntegrationTest {

	@Autowired
	private CancelSubscriptionUseCase cancelSubscriptionUseCase;

	@Autowired
	private ExpireDueSubscriptionsUseCase expireDueSubscriptionsUseCase;

	@Autowired
	private CheckContentAccessQueryUseCase checkContentAccessQueryUseCase;

	@Autowired
	private SaveMembershipPort saveMembershipPort;

	@Autowired
	private SaveSubscriptionPort saveSubscriptionPort;

	@Autowired
	private LoadSubscriptionPort loadSubscriptionPort;

	@Autowired
	private InMemoryEntitlementCacheAdapter entitlementCacheAdapter;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private final MemberUuid memberUuid = new MemberUuid(UUID.fromString("11111111-1111-1111-1111-111111111111"));
	private final CreatorUuid creatorUuid = new CreatorUuid(UUID.fromString("22222222-2222-2222-2222-222222222222"));
	private final MembershipUuid membershipUuid = new MembershipUuid(UUID.fromString("99999999-9999-9999-9999-999999999999"));

	@BeforeEach
	void setUp() {
		entitlementCacheAdapter.clear();
	}

	@Test
	void cancelMakesSubscriptionInactiveRemovesCacheAndDeniesAccess() {
		SubscriptionUuid subscriptionUuid = new SubscriptionUuid(UUID.fromString("55555555-5555-5555-5555-555555555555"));
		saveApprovedMembership();
		saveSubscriptionPort.save(MembershipSubscription.active(
				subscriptionUuid,
				membershipUuid,
				memberUuid,
				Instant.parse("2026-08-22T00:01:00Z")
		));
		entitlementCacheAdapter.save(Entitlement.cachedActive(memberUuid, creatorUuid));

		cancelSubscriptionUseCase.cancel(new CancelSubscriptionCommand(
				memberUuid,
				subscriptionUuid,
				Instant.parse("2026-08-22T00:10:00Z")
		));

		MembershipSubscription canceled = loadSubscriptionPort.findByUuid(subscriptionUuid).orElseThrow();
		assertThat(canceled.status()).isEqualTo(SubscriptionStatus.INACTIVE);
		assertThat(canceled.endedAt()).isEqualTo(Instant.parse("2026-08-22T00:10:00Z"));
		assertThat(entitlementCacheAdapter.find(memberUuid, creatorUuid)).isEmpty();
		assertThat(loadSubscriptionPort.findCurrentByMemberAndCreator(memberUuid, creatorUuid)).isEmpty();
		assertThat(eventTypes(subscriptionUuid)).containsExactly(MembershipEventTypes.MEMBERSHIP_CANCELED);
		ContentAccessResult access = checkContentAccessQueryUseCase.check(
				new CheckContentAccessQuery(memberUuid, creatorUuid)
		);
		assertThat(access.allowed()).isFalse();
	}

	@Test
	void expireDueDeactivatesOldActiveSubscriptionOnly() {
		SubscriptionUuid dueUuid = new SubscriptionUuid(UUID.fromString("55555555-5555-5555-5555-555555555555"));
		SubscriptionUuid recentUuid = new SubscriptionUuid(UUID.fromString("66666666-6666-6666-6666-666666666666"));
		Instant now = Instant.parse("2026-08-22T00:00:00Z");
		saveApprovedMembership();
		saveSubscriptionPort.save(MembershipSubscription.active(
				dueUuid,
				membershipUuid,
				memberUuid,
				Instant.parse("2026-07-22T00:00:00Z")
		));
		saveSubscriptionPort.save(MembershipSubscription.active(
				recentUuid,
				membershipUuid,
				new MemberUuid(UUID.fromString("33333333-3333-3333-3333-333333333333")),
				Instant.parse("2026-08-20T00:00:00Z")
		));

		int expiredCount = expireDueSubscriptionsUseCase.expireDue(now);

		assertThat(expiredCount).isEqualTo(1);
		assertThat(loadSubscriptionPort.findByUuid(dueUuid).orElseThrow().status()).isEqualTo(SubscriptionStatus.INACTIVE);
		assertThat(loadSubscriptionPort.findByUuid(recentUuid).orElseThrow().status()).isEqualTo(SubscriptionStatus.ACTIVE);
		assertThat(eventTypes(dueUuid)).containsExactly(MembershipEventTypes.MEMBERSHIP_EXPIRED);
		assertThat(eventTypes(recentUuid)).isEmpty();
	}

	private void saveApprovedMembership() {
		saveMembershipPort.save(Membership.apply(
				membershipUuid,
				creatorUuid,
				"테스트 멤버십",
				"설명",
				9900,
				Instant.parse("2026-07-01T00:00:00Z")
		).approve(AdminUuid.from("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")));
	}

	private List<String> eventTypes(SubscriptionUuid subscriptionUuid) {
		return jdbcTemplate.queryForList(
				"select event_type from membership_outbox where aggregate_uuid = ?",
				String.class,
				subscriptionUuid.value().toString()
		);
	}
}
