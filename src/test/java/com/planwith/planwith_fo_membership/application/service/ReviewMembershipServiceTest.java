package com.planwith.planwith_fo_membership.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_membership.adapter.out.persistence.membership.InMemoryLoadMembershipPort;
import com.planwith.planwith_fo_membership.application.command.ApproveMembershipCommand;
import com.planwith.planwith_fo_membership.application.command.RejectMembershipCommand;
import com.planwith.planwith_fo_membership.application.query.ReviewMembershipResult;
import com.planwith.planwith_fo_membership.domain.exception.InvalidMembershipStateException;
import com.planwith.planwith_fo_membership.domain.exception.MembershipNotFoundException;
import com.planwith.planwith_fo_membership.domain.model.Membership;
import com.planwith.planwith_fo_membership.domain.model.MembershipStatus;
import com.planwith.planwith_fo_membership.domain.model.vo.AdminUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;
import com.planwith.planwith_fo_membership.domain.service.MembershipPolicy;

class ReviewMembershipServiceTest {

	private final MembershipUuid membershipUuid = MembershipUuid.from("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
	private final CreatorUuid creatorUuid = CreatorUuid.from("22222222-2222-2222-2222-222222222222");
	private final AdminUuid adminUuid = AdminUuid.from("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

	private InMemoryLoadMembershipPort membershipPort;
	private ReviewMembershipService service;

	@BeforeEach
	void setUp() {
		membershipPort = new InMemoryLoadMembershipPort();
		service = new ReviewMembershipService(membershipPort, membershipPort);
	}

	@Test
	void approvePendingMembershipStoresAdminAndApprovedStatus() {
		membershipPort.save(pending());

		ReviewMembershipResult result = service.approve(new ApproveMembershipCommand(membershipUuid, adminUuid));
		Membership saved = membershipPort.findByUuid(membershipUuid).orElseThrow();

		assertThat(result.status()).isEqualTo(MembershipStatus.APPROVED);
		assertThat(result.adminUuid()).isEqualTo(adminUuid);
		assertThat(result.rejectReason()).isNull();
		assertThat(saved.status()).isEqualTo(MembershipStatus.APPROVED);
		assertThat(saved.adminUuid()).isEqualTo(adminUuid);
		assertThat(MembershipPolicy.canAcceptSubscription(saved)).isTrue();
	}

	@Test
	void rejectPendingMembershipStoresAdminAndReason() {
		membershipPort.save(pending());

		ReviewMembershipResult result = service.reject(
				new RejectMembershipCommand(membershipUuid, adminUuid, "서류 미비")
		);
		Membership saved = membershipPort.findByUuid(membershipUuid).orElseThrow();

		assertThat(result.status()).isEqualTo(MembershipStatus.REJECTED);
		assertThat(result.adminUuid()).isEqualTo(adminUuid);
		assertThat(result.rejectReason()).isEqualTo("서류 미비");
		assertThat(saved.status()).isEqualTo(MembershipStatus.REJECTED);
		assertThat(MembershipPolicy.canAcceptSubscription(saved)).isFalse();
	}

	@Test
	void cannotApproveAlreadyApprovedMembership() {
		membershipPort.save(pending().approve(adminUuid));

		assertThatThrownBy(() -> service.approve(new ApproveMembershipCommand(membershipUuid, adminUuid)))
				.isInstanceOf(InvalidMembershipStateException.class);
	}

	@Test
	void cannotRejectAlreadyRejectedMembership() {
		membershipPort.save(pending().reject(adminUuid, "서류 미비"));

		assertThatThrownBy(() -> service.reject(new RejectMembershipCommand(membershipUuid, adminUuid, "재거절")))
				.isInstanceOf(InvalidMembershipStateException.class);
	}

	@Test
	void throwsWhenMembershipDoesNotExist() {
		assertThatThrownBy(() -> service.approve(
				new ApproveMembershipCommand(new MembershipUuid(UUID.randomUUID()), adminUuid)
		)).isInstanceOf(MembershipNotFoundException.class);
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
