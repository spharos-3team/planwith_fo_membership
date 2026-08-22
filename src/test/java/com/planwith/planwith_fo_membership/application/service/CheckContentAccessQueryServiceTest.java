package com.planwith.planwith_fo_membership.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_membership.adapter.out.redis.InMemoryEntitlementCacheAdapter;
import com.planwith.planwith_fo_membership.application.port.in.query.CheckContentAccessQueryUseCase;
import com.planwith.planwith_fo_membership.application.port.out.SaveMembershipPort;
import com.planwith.planwith_fo_membership.application.port.out.SaveSubscriptionPort;
import com.planwith.planwith_fo_membership.application.query.CheckContentAccessQuery;
import com.planwith.planwith_fo_membership.application.query.ContentAccessResult;
import com.planwith.planwith_fo_membership.domain.model.Membership;
import com.planwith.planwith_fo_membership.domain.model.Subscription;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CheckContentAccessQueryServiceTest {

	@Autowired
	private CheckContentAccessQueryUseCase checkContentAccessQueryUseCase;

	@Autowired
	private SaveMembershipPort saveMembershipPort;

	@Autowired
	private SaveSubscriptionPort saveSubscriptionPort;

	@Autowired
	private InMemoryEntitlementCacheAdapter entitlementCacheAdapter;

	private final MemberUuid memberUuid = new MemberUuid(UUID.fromString("11111111-1111-1111-1111-111111111111"));
	private final CreatorUuid creatorUuid = new CreatorUuid(UUID.fromString("22222222-2222-2222-2222-222222222222"));
	private final MembershipUuid membershipUuid = new MembershipUuid(UUID.fromString("99999999-9999-9999-9999-999999999999"));

	@BeforeEach
	void setUp() {
		entitlementCacheAdapter.clear();
	}

	@Test
	void deniesAccessWhenSubscriptionDoesNotExist() {
		ContentAccessResult result = checkContentAccessQueryUseCase.check(
				new CheckContentAccessQuery(memberUuid, creatorUuid)
		);

		assertThat(result.allowed()).isFalse();
	}

	@Test
	void allowsAccessWhenActiveSubscriptionExists() {
		saveMembershipPort.save(Membership.restore(
				membershipUuid,
				null,
				creatorUuid,
				"테스트 멤버십",
				null,
				9900,
				"APPROVED",
				null,
				Instant.parse("2026-08-22T00:00:00Z")
		));
		saveSubscriptionPort.save(Subscription.active(
				new SubscriptionUuid(UUID.fromString("77777777-7777-7777-7777-777777777777")),
				membershipUuid,
				memberUuid,
				Instant.parse("2026-08-22T00:01:00Z")
		));

		ContentAccessResult result = checkContentAccessQueryUseCase.check(
				new CheckContentAccessQuery(memberUuid, creatorUuid)
		);

		assertThat(result.allowed()).isTrue();
		assertThat(entitlementCacheAdapter.find(memberUuid, creatorUuid)).isPresent();
	}
}
