package com.planwith.planwith_fo_membership.adapter.out.persistence.subscription;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_membership.application.port.out.LoadSubscriptionPort;
import com.planwith.planwith_fo_membership.application.port.out.SaveMembershipPort;
import com.planwith.planwith_fo_membership.application.port.out.SaveSubscriptionPort;
import com.planwith.planwith_fo_membership.application.query.JoinedMembershipResult;
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
class MembershipSubscriptionPersistenceAdapterIntegrationTest {

	@Autowired
	private SaveMembershipPort saveMembershipPort;

	@Autowired
	private SaveSubscriptionPort saveSubscriptionPort;

	@Autowired
	private LoadSubscriptionPort loadSubscriptionPort;

	@Test
	void saveAndLoadActiveSubscription() {
		MemberUuid memberUuid = new MemberUuid(UUID.fromString("11111111-1111-1111-1111-111111111111"));
		CreatorUuid creatorUuid = new CreatorUuid(UUID.fromString("22222222-2222-2222-2222-222222222222"));
		MembershipUuid membershipUuid = new MembershipUuid(UUID.fromString("99999999-9999-9999-9999-999999999999"));
		SubscriptionUuid subscriptionUuid = new SubscriptionUuid(UUID.fromString("55555555-5555-5555-5555-555555555555"));

		saveMembershipPort.save(Membership.apply(
				membershipUuid,
				creatorUuid,
				"테스트 멤버십",
				"설명",
				9900,
				Instant.parse("2026-08-22T00:00:00Z")
		).approve(AdminUuid.from("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")));
		saveSubscriptionPort.save(MembershipSubscription.active(
				subscriptionUuid,
				membershipUuid,
				memberUuid,
				Instant.parse("2026-08-22T00:01:00Z")
		));

		MembershipSubscription loaded = loadSubscriptionPort.findCurrentByMemberAndCreator(memberUuid, creatorUuid)
				.orElseThrow();
		assertThat(loaded.status()).isEqualTo(SubscriptionStatus.ACTIVE);
		assertThat(loaded.subscriptionUuid()).isEqualTo(subscriptionUuid);
		assertThat(loaded.membershipUuid()).isEqualTo(membershipUuid);
	}

	@Test
	void loadJoinedMembershipsByMember() {
		MemberUuid memberUuid = new MemberUuid(UUID.fromString("11111111-1111-1111-1111-111111111111"));
		CreatorUuid creatorUuid = new CreatorUuid(UUID.fromString("22222222-2222-2222-2222-222222222222"));
		MembershipUuid membershipUuid = new MembershipUuid(UUID.fromString("99999999-9999-9999-9999-999999999999"));
		SubscriptionUuid subscriptionUuid = new SubscriptionUuid(UUID.fromString("55555555-5555-5555-5555-555555555555"));

		saveApprovedMembership(membershipUuid, creatorUuid);
		saveSubscriptionPort.save(MembershipSubscription.active(
				subscriptionUuid,
				membershipUuid,
				memberUuid,
				Instant.parse("2026-08-22T00:01:00Z")
		));

		List<JoinedMembershipResult> joined = loadSubscriptionPort.findJoinedByMember(memberUuid);
		assertThat(joined).hasSize(1);
		assertThat(joined.get(0).subscriptionUuid()).isEqualTo(subscriptionUuid);
		assertThat(joined.get(0).creatorUuid()).isEqualTo(creatorUuid);
		assertThat(joined.get(0).membershipName()).isEqualTo("테스트 멤버십");
		assertThat(joined.get(0).monthlyPrice()).isEqualTo(9900);
	}

	@Test
	void loadActiveSubscribersByCreator() {
		MemberUuid memberUuid = new MemberUuid(UUID.fromString("11111111-1111-1111-1111-111111111111"));
		CreatorUuid creatorUuid = new CreatorUuid(UUID.fromString("22222222-2222-2222-2222-222222222222"));
		MembershipUuid membershipUuid = new MembershipUuid(UUID.fromString("99999999-9999-9999-9999-999999999999"));
		SubscriptionUuid activeUuid = new SubscriptionUuid(UUID.fromString("55555555-5555-5555-5555-555555555555"));
		SubscriptionUuid inactiveUuid = new SubscriptionUuid(UUID.fromString("77777777-7777-7777-7777-777777777777"));

		saveApprovedMembership(membershipUuid, creatorUuid);
		saveSubscriptionPort.save(MembershipSubscription.active(
				activeUuid,
				membershipUuid,
				memberUuid,
				Instant.parse("2026-08-22T00:01:00Z")
		));
		saveSubscriptionPort.save(MembershipSubscription.active(
				inactiveUuid,
				membershipUuid,
				new MemberUuid(UUID.fromString("33333333-3333-3333-3333-333333333333")),
				Instant.parse("2026-08-22T00:02:00Z")
		).deactivate(Instant.parse("2026-08-22T00:03:00Z")));

		List<MembershipSubscription> subscribers = loadSubscriptionPort.findActiveByCreator(creatorUuid);
		assertThat(subscribers).hasSize(1);
		assertThat(subscribers.get(0).subscriptionUuid()).isEqualTo(activeUuid);
		assertThat(subscribers.get(0).memberUuid()).isEqualTo(memberUuid);
	}

	private void saveApprovedMembership(MembershipUuid membershipUuid, CreatorUuid creatorUuid) {
		saveMembershipPort.save(Membership.apply(
				membershipUuid,
				creatorUuid,
				"테스트 멤버십",
				"설명",
				9900,
				Instant.parse("2026-08-22T00:00:00Z")
		).approve(AdminUuid.from("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")));
	}
}
