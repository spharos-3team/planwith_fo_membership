package com.planwith.planwith_fo_membership.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_membership.adapter.out.follow.InMemoryFollowQueryAdapter;
import com.planwith.planwith_fo_membership.application.command.ValidateJoinEligibilityCommand;
import com.planwith.planwith_fo_membership.application.port.in.command.ValidateJoinEligibilityUseCase;
import com.planwith.planwith_fo_membership.application.port.out.SaveMembershipPort;
import com.planwith.planwith_fo_membership.application.port.out.SaveSubscriptionPort;
import com.planwith.planwith_fo_membership.application.query.ValidateJoinEligibilityResult;
import com.planwith.planwith_fo_membership.domain.exception.DuplicateSubscriptionException;
import com.planwith.planwith_fo_membership.domain.exception.FollowRequiredException;
import com.planwith.planwith_fo_membership.domain.model.Membership;
import com.planwith.planwith_fo_membership.domain.model.MembershipSubscription;
import com.planwith.planwith_fo_membership.domain.model.vo.AdminUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(ValidateJoinEligibilityServiceIntegrationTest.FollowQueryTestConfig.class)
class ValidateJoinEligibilityServiceIntegrationTest {

	private final MemberUuid memberUuid = MemberUuid.from("11111111-1111-1111-1111-111111111111");
	private final CreatorUuid creatorUuid = CreatorUuid.from("22222222-2222-2222-2222-222222222222");
	private final MembershipUuid membershipUuid = MembershipUuid.from("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

	@Autowired
	private ValidateJoinEligibilityUseCase validateJoinEligibilityUseCase;

	@Autowired
	private SaveMembershipPort saveMembershipPort;

	@Autowired
	private SaveSubscriptionPort saveSubscriptionPort;

	@Autowired
	private InMemoryFollowQueryAdapter followQueryPort;

	@BeforeEach
	void setUp() {
		followQueryPort.clear();
		saveMembershipPort.save(Membership.apply(
				membershipUuid,
				creatorUuid,
				"윤휘명의 여행 멤버십",
				"월간 멤버십",
				100,
				Instant.parse("2026-08-22T00:00:00Z")
		).approve(AdminUuid.from("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")));
	}

	@Test
	void followerCanJoinApprovedMembership() {
		followQueryPort.follow(memberUuid, creatorUuid);

		ValidateJoinEligibilityResult result = validateJoinEligibilityUseCase.validate(
				new ValidateJoinEligibilityCommand(memberUuid, creatorUuid)
		);

		assertThat(result.eligible()).isTrue();
		assertThat(result.following()).isTrue();
		assertThat(result.membershipUuid()).isEqualTo(membershipUuid);
		assertThat(result.monthlyPrice()).isEqualTo(100);
	}

	@Test
	void nonFollowerCannotJoin() {
		assertThatThrownBy(() -> validateJoinEligibilityUseCase.validate(
				new ValidateJoinEligibilityCommand(memberUuid, creatorUuid)
		)).isInstanceOf(FollowRequiredException.class);
	}

	@Test
	void activeSubscriberCannotJoinAgain() {
		followQueryPort.follow(memberUuid, creatorUuid);
		saveSubscriptionPort.save(MembershipSubscription.active(
				SubscriptionUuid.from("55555555-5555-5555-5555-555555555555"),
				membershipUuid,
				memberUuid,
				Instant.parse("2026-08-22T00:01:00Z")
		));

		assertThatThrownBy(() -> validateJoinEligibilityUseCase.validate(
				new ValidateJoinEligibilityCommand(memberUuid, creatorUuid)
		)).isInstanceOf(DuplicateSubscriptionException.class);
	}

	@TestConfiguration
	static class FollowQueryTestConfig {

		@Bean
		@Primary
		InMemoryFollowQueryAdapter inMemoryFollowQueryAdapter() {
			return new InMemoryFollowQueryAdapter();
		}
	}
}
