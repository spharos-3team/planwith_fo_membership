package com.planwith.planwith_fo_membership.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

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
import com.planwith.planwith_fo_membership.application.command.HandleTokenDeductionFailedCommand;
import com.planwith.planwith_fo_membership.application.command.HandleTokenDeductionSucceededCommand;
import com.planwith.planwith_fo_membership.application.command.StartTokenPaymentCommand;
import com.planwith.planwith_fo_membership.application.port.in.command.HandleTokenDeductionFailedUseCase;
import com.planwith.planwith_fo_membership.application.port.in.command.HandleTokenDeductionSucceededUseCase;
import com.planwith.planwith_fo_membership.application.port.in.command.StartTokenPaymentUseCase;
import com.planwith.planwith_fo_membership.application.port.out.LoadPaymentPort;
import com.planwith.planwith_fo_membership.application.port.out.LoadRevenuePort;
import com.planwith.planwith_fo_membership.application.port.out.LoadSubscriptionPort;
import com.planwith.planwith_fo_membership.application.port.out.SaveMembershipPort;
import com.planwith.planwith_fo_membership.application.port.out.SaveRevenuePort;
import com.planwith.planwith_fo_membership.application.query.StartTokenPaymentResult;
import com.planwith.planwith_fo_membership.domain.model.Membership;
import com.planwith.planwith_fo_membership.domain.model.MembershipRevenue;
import com.planwith.planwith_fo_membership.domain.model.PaymentStatus;
import com.planwith.planwith_fo_membership.domain.model.SubscriptionStatus;
import com.planwith.planwith_fo_membership.domain.model.vo.AdminUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.RevenueUuid;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(HandleTokenDeductionSagaServiceIntegrationTest.FollowQueryTestConfig.class)
class HandleTokenDeductionSagaServiceIntegrationTest {

	private final MemberUuid memberUuid = MemberUuid.from("11111111-1111-1111-1111-111111111111");
	private final CreatorUuid creatorUuid = CreatorUuid.from("22222222-2222-2222-2222-222222222222");

	@Autowired
	private StartTokenPaymentUseCase startTokenPaymentUseCase;

	@Autowired
	private HandleTokenDeductionSucceededUseCase handleTokenDeductionSucceededUseCase;

	@Autowired
	private HandleTokenDeductionFailedUseCase handleTokenDeductionFailedUseCase;

	@Autowired
	private SaveMembershipPort saveMembershipPort;

	@Autowired
	private SaveRevenuePort saveRevenuePort;

	@Autowired
	private LoadPaymentPort loadPaymentPort;

	@Autowired
	private LoadSubscriptionPort loadSubscriptionPort;

	@Autowired
	private LoadRevenuePort loadRevenuePort;

	@Autowired
	private InMemoryFollowQueryAdapter followQueryPort;

	@BeforeEach
	void setUp() {
		followQueryPort.clear();
		saveMembershipPort.save(Membership.apply(
				MembershipUuid.from("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
				creatorUuid,
				"윤휘명의 여행 멤버십",
				"월간 멤버십",
				100,
				Instant.parse("2026-08-22T00:00:00Z")
		).approve(AdminUuid.from("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")));
		saveRevenuePort.save(MembershipRevenue.empty(
				RevenueUuid.from("66666666-6666-6666-6666-666666666666"),
				creatorUuid
		));
		followQueryPort.follow(memberUuid, creatorUuid);
	}

	@Test
	void successPersistsActiveSubscriptionAndRevenue() {
		StartTokenPaymentResult started = startTokenPaymentUseCase.start(
				new StartTokenPaymentCommand(memberUuid, creatorUuid)
		);

		handleTokenDeductionSucceededUseCase.handle(new HandleTokenDeductionSucceededCommand(
				UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"),
				memberUuid,
				creatorUuid,
				started.paymentUuid(),
				started.subscriptionUuid(),
				Instant.parse("2026-08-22T00:10:00Z")
		));

		assertThat(loadPaymentPort.findByUuid(started.paymentUuid()).orElseThrow().paymentStatus())
				.isEqualTo(PaymentStatus.SUCCESS);
		assertThat(loadSubscriptionPort.findByUuid(started.subscriptionUuid()).orElseThrow().status())
				.isEqualTo(SubscriptionStatus.ACTIVE);
		assertThat(loadRevenuePort.findByCreator(creatorUuid).orElseThrow().availableRevenue()).isEqualTo(100L);
	}

	@Test
	void failureDoesNotCreateSubscription() {
		StartTokenPaymentResult started = startTokenPaymentUseCase.start(
				new StartTokenPaymentCommand(memberUuid, creatorUuid)
		);

		handleTokenDeductionFailedUseCase.handle(new HandleTokenDeductionFailedCommand(
				UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"),
				memberUuid,
				started.paymentUuid(),
				started.subscriptionUuid(),
				Instant.parse("2026-08-22T00:10:00Z")
		));

		assertThat(loadPaymentPort.findByUuid(started.paymentUuid()).orElseThrow().paymentStatus())
				.isEqualTo(PaymentStatus.FAILED);
		assertThat(loadSubscriptionPort.findByUuid(started.subscriptionUuid())).isEmpty();
		assertThat(loadRevenuePort.findByCreator(creatorUuid).orElseThrow().totalRevenue()).isZero();
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
