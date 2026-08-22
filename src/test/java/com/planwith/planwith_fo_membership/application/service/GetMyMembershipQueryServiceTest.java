package com.planwith.planwith_fo_membership.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_membership.adapter.out.persistence.membership.InMemoryLoadMembershipPort;
import com.planwith.planwith_fo_membership.application.query.GetMyMembershipQuery;
import com.planwith.planwith_fo_membership.application.query.MyMembershipResult;
import com.planwith.planwith_fo_membership.domain.model.Membership;
import com.planwith.planwith_fo_membership.domain.model.MembershipStatus;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;
import com.planwith.planwith_fo_membership.domain.service.MembershipApplicationPolicy;

class GetMyMembershipQueryServiceTest {

	private final CreatorUuid creatorUuid = CreatorUuid.from("22222222-2222-2222-2222-222222222222");
	private final MembershipUuid membershipUuid = MembershipUuid.from("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

	private InMemoryLoadMembershipPort membershipPort;
	private GetMyMembershipQueryService service;

	@BeforeEach
	void setUp() {
		membershipPort = new InMemoryLoadMembershipPort();
		service = new GetMyMembershipQueryService(membershipPort);
	}

	@Test
	void returnsEmptyWhenCreatorHasNoMembership() {
		assertThat(service.get(new GetMyMembershipQuery(creatorUuid))).isEmpty();
	}

	@Test
	void returnsMembershipStatusAndPrice() {
		membershipPort.save(Membership.apply(
				membershipUuid,
				creatorUuid,
				"윤휘명의 여행 멤버십",
				"월간 멤버십",
				100,
				Instant.parse("2026-08-22T00:00:00Z")
		));

		MyMembershipResult result = service.get(new GetMyMembershipQuery(creatorUuid)).orElseThrow();

		assertThat(result.membershipUuid()).isEqualTo(membershipUuid);
		assertThat(result.membershipName()).isEqualTo("윤휘명의 여행 멤버십");
		assertThat(result.monthlyPrice()).isEqualTo(100);
		assertThat(result.priceUnit()).isEqualTo(MembershipApplicationPolicy.PRICE_UNIT_TOKEN);
		assertThat(result.status()).isEqualTo(MembershipStatus.PENDING);
		assertThat(result.rejectReason()).isNull();
	}
}
