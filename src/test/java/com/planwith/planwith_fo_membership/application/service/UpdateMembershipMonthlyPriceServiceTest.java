package com.planwith.planwith_fo_membership.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_membership.adapter.out.persistence.membership.InMemoryLoadMembershipPort;
import com.planwith.planwith_fo_membership.application.command.UpdateMembershipCommand;
import com.planwith.planwith_fo_membership.application.query.UpdateMembershipMonthlyPriceResult;
import com.planwith.planwith_fo_membership.domain.exception.InvalidMembershipPriceException;
import com.planwith.planwith_fo_membership.domain.exception.InvalidMembershipStateException;
import com.planwith.planwith_fo_membership.domain.exception.MembershipNotFoundException;
import com.planwith.planwith_fo_membership.domain.model.Membership;
import com.planwith.planwith_fo_membership.domain.model.MembershipStatus;
import com.planwith.planwith_fo_membership.domain.model.vo.AdminUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;

class UpdateMembershipMonthlyPriceServiceTest {

	private final MembershipUuid membershipUuid = MembershipUuid.from("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
	private final CreatorUuid creatorUuid = CreatorUuid.from("22222222-2222-2222-2222-222222222222");
	private final AdminUuid adminUuid = AdminUuid.from("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

	private InMemoryLoadMembershipPort membershipPort;
	private UpdateMembershipMonthlyPriceService service;

	@BeforeEach
	void setUp() {
		membershipPort = new InMemoryLoadMembershipPort();
		service = new UpdateMembershipMonthlyPriceService(membershipPort, membershipPort);
	}

	@Test
	void updateApprovedMembershipMonthlyPrice() {
		membershipPort.save(pending().approve(adminUuid));

		UpdateMembershipMonthlyPriceResult result = service.update(
				new UpdateMembershipCommand(creatorUuid, 40)
		);

		assertThat(result.monthlyPrice()).isEqualTo(40);
		assertThat(result.status()).isEqualTo(MembershipStatus.APPROVED);
		assertThat(membershipPort.findByUuid(membershipUuid).orElseThrow().monthlyPrice()).isEqualTo(40);
	}

	@Test
	void rejectsPriceBelowMinimum() {
		membershipPort.save(pending().approve(adminUuid));

		assertThatThrownBy(() -> service.update(new UpdateMembershipCommand(creatorUuid, 9)))
				.isInstanceOf(InvalidMembershipPriceException.class);
	}

	@Test
	void rejectsPendingMembership() {
		membershipPort.save(pending());

		assertThatThrownBy(() -> service.update(new UpdateMembershipCommand(creatorUuid, 40)))
				.isInstanceOf(InvalidMembershipStateException.class);
	}

	@Test
	void throwsWhenMembershipMissing() {
		assertThatThrownBy(() -> service.update(new UpdateMembershipCommand(creatorUuid, 40)))
				.isInstanceOf(MembershipNotFoundException.class);
	}

	private Membership pending() {
		return Membership.apply(
				membershipUuid,
				creatorUuid,
				"윤휘명의 여행 멤버십",
				"월간 멤버십",
				100,
				Instant.parse("2026-08-22T00:00:00Z")
		);
	}
}
