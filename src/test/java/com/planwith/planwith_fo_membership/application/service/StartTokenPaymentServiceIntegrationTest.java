package com.planwith.planwith_fo_membership.application.service;

import static org.assertj.core.api.Assertions.assertThat;

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
import com.planwith.planwith_fo_membership.application.command.StartTokenPaymentCommand;
import com.planwith.planwith_fo_membership.application.port.in.command.StartTokenPaymentUseCase;
import com.planwith.planwith_fo_membership.application.port.out.LoadMembershipSagaPort;
import com.planwith.planwith_fo_membership.application.port.out.LoadPaymentPort;
import com.planwith.planwith_fo_membership.application.port.out.SaveMembershipPort;
import com.planwith.planwith_fo_membership.application.query.StartTokenPaymentResult;
import com.planwith.planwith_fo_membership.domain.model.Membership;
import com.planwith.planwith_fo_membership.domain.model.MembershipPayment;
import com.planwith.planwith_fo_membership.domain.model.MembershipSaga;
import com.planwith.planwith_fo_membership.domain.model.MembershipSagaStatus;
import com.planwith.planwith_fo_membership.domain.model.PaymentStatus;
import com.planwith.planwith_fo_membership.domain.model.vo.AdminUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(StartTokenPaymentServiceIntegrationTest.FollowQueryTestConfig.class)
class StartTokenPaymentServiceIntegrationTest {

	private final MemberUuid memberUuid = MemberUuid.from("11111111-1111-1111-1111-111111111111");
	private final CreatorUuid creatorUuid = CreatorUuid.from("22222222-2222-2222-2222-222222222222");

	@Autowired
	private StartTokenPaymentUseCase startTokenPaymentUseCase;

	@Autowired
	private SaveMembershipPort saveMembershipPort;

	@Autowired
	private LoadPaymentPort loadPaymentPort;

	@Autowired
	private LoadMembershipSagaPort loadMembershipSagaPort;

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
		followQueryPort.follow(memberUuid, creatorUuid);
	}

	@Test
	void persistsReadyPaymentSagaAndTokenDeductionOutboxTogether() {
		StartTokenPaymentResult result = startTokenPaymentUseCase.start(
				new StartTokenPaymentCommand(memberUuid, creatorUuid)
		);

		MembershipPayment payment = loadPaymentPort.findByUuid(result.paymentUuid()).orElseThrow();
		MembershipSaga saga = loadMembershipSagaPort.findInProgressByMemberAndCreator(memberUuid, creatorUuid)
				.orElseThrow();

		assertThat(payment.paymentStatus()).isEqualTo(PaymentStatus.READY);
		assertThat(payment.amount()).isEqualTo(100L);
		assertThat(saga.status()).isEqualTo(MembershipSagaStatus.PAYMENT_PENDING);
		assertThat(saga.paymentUuid()).isEqualTo(result.paymentUuid());
	}

	@Test
	void secondStartReusesReadyPayment() {
		StartTokenPaymentResult first = startTokenPaymentUseCase.start(
				new StartTokenPaymentCommand(memberUuid, creatorUuid)
		);
		StartTokenPaymentResult second = startTokenPaymentUseCase.start(
				new StartTokenPaymentCommand(memberUuid, creatorUuid)
		);

		assertThat(second.paymentUuid()).isEqualTo(first.paymentUuid());
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
