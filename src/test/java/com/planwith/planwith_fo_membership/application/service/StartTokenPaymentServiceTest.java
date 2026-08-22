package com.planwith.planwith_fo_membership.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_membership.adapter.out.follow.InMemoryFollowQueryAdapter;
import com.planwith.planwith_fo_membership.adapter.out.persistence.membership.InMemoryLoadMembershipPort;
import com.planwith.planwith_fo_membership.adapter.out.persistence.outbox.InMemoryMembershipEventOutboxPort;
import com.planwith.planwith_fo_membership.adapter.out.persistence.payment.InMemoryPaymentPort;
import com.planwith.planwith_fo_membership.adapter.out.persistence.saga.InMemoryMembershipSagaPort;
import com.planwith.planwith_fo_membership.adapter.out.persistence.subscription.InMemoryLoadSubscriptionPort;
import com.planwith.planwith_fo_membership.application.command.StartTokenPaymentCommand;
import com.planwith.planwith_fo_membership.application.query.StartTokenPaymentResult;
import com.planwith.planwith_fo_membership.domain.event.MembershipEventTypes;
import com.planwith.planwith_fo_membership.domain.exception.FollowRequiredException;
import com.planwith.planwith_fo_membership.domain.model.Membership;
import com.planwith.planwith_fo_membership.domain.model.PaymentStatus;
import com.planwith.planwith_fo_membership.domain.model.vo.AdminUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;
import com.planwith.planwith_fo_membership.domain.service.MembershipApplicationPolicy;

class StartTokenPaymentServiceTest {

	private final MemberUuid memberUuid = MemberUuid.from("11111111-1111-1111-1111-111111111111");
	private final CreatorUuid creatorUuid = CreatorUuid.from("22222222-2222-2222-2222-222222222222");
	private final MembershipUuid membershipUuid = MembershipUuid.from("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

	private InMemoryFollowQueryAdapter followQueryPort;
	private InMemoryPaymentPort paymentPort;
	private InMemoryMembershipEventOutboxPort outboxPort;
	private StartTokenPaymentService service;

	@BeforeEach
	void setUp() {
		InMemoryLoadMembershipPort membershipPort = new InMemoryLoadMembershipPort();
		followQueryPort = new InMemoryFollowQueryAdapter();
		InMemoryLoadSubscriptionPort subscriptionPort = new InMemoryLoadSubscriptionPort();
		paymentPort = new InMemoryPaymentPort();
		InMemoryMembershipSagaPort sagaPort = new InMemoryMembershipSagaPort();
		outboxPort = new InMemoryMembershipEventOutboxPort();
		ValidateJoinEligibilityService validateService = new ValidateJoinEligibilityService(
				membershipPort,
				followQueryPort,
				subscriptionPort
		);
		service = new StartTokenPaymentService(
				validateService,
				sagaPort,
				paymentPort,
				paymentPort,
				sagaPort,
				outboxPort,
				new ObjectMapper().findAndRegisterModules()
		);
		membershipPort.save(Membership.apply(
				membershipUuid,
				creatorUuid,
				"윤휘명의 여행 멤버십",
				"월간 멤버십",
				100,
				Instant.parse("2026-08-22T00:00:00Z")
		).approve(AdminUuid.from("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")));
	}

	@Test
	void startsReadyPaymentForExactMembershipPriceAndWritesOutbox() {
		followQueryPort.follow(memberUuid, creatorUuid);

		StartTokenPaymentResult result = service.start(command());

		assertThat(result.status()).isEqualTo(PaymentStatus.READY);
		assertThat(result.amount()).isEqualTo(100L);
		assertThat(result.priceUnit()).isEqualTo(MembershipApplicationPolicy.PRICE_UNIT_TOKEN);
		assertThat(paymentPort.findByUuid(result.paymentUuid()).orElseThrow().paymentStatus())
				.isEqualTo(PaymentStatus.READY);
		assertThat(outboxPort.messages()).hasSize(1);
		assertThat(outboxPort.messages().get(0).eventType())
				.isEqualTo(MembershipEventTypes.TOKEN_DEDUCTION_REQUESTED);
		assertThat(outboxPort.messages().get(0).payload()).contains("\"amount\":100");
		assertThat(outboxPort.messages().get(0).payload()).contains("\"priceUnit\":\"TOKEN\"");
	}

	@Test
	void reusesExistingReadyPaymentWithoutExtraOutbox() {
		followQueryPort.follow(memberUuid, creatorUuid);

		StartTokenPaymentResult first = service.start(command());
		StartTokenPaymentResult second = service.start(command());

		assertThat(second.paymentUuid()).isEqualTo(first.paymentUuid());
		assertThat(second.subscriptionUuid()).isEqualTo(first.subscriptionUuid());
		assertThat(outboxPort.messages()).hasSize(1);
	}

	@Test
	void doesNotStartPaymentWhenFollowIsRequired() {
		assertThatThrownBy(() -> service.start(command()))
				.isInstanceOf(FollowRequiredException.class);
		assertThat(outboxPort.messages()).isEmpty();
	}

	private StartTokenPaymentCommand command() {
		return new StartTokenPaymentCommand(memberUuid, creatorUuid);
	}
}
