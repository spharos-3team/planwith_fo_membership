package com.planwith.planwith_fo_membership.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_membership.application.command.ApproveMembershipCommand;
import com.planwith.planwith_fo_membership.application.command.RejectMembershipCommand;
import com.planwith.planwith_fo_membership.application.port.in.command.ApproveMembershipUseCase;
import com.planwith.planwith_fo_membership.application.port.in.command.RejectMembershipUseCase;
import com.planwith.planwith_fo_membership.application.port.out.LoadMembershipPort;
import com.planwith.planwith_fo_membership.application.port.out.SaveMembershipPort;
import com.planwith.planwith_fo_membership.domain.model.Membership;
import com.planwith.planwith_fo_membership.domain.model.MembershipStatus;
import com.planwith.planwith_fo_membership.domain.model.vo.AdminUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReviewMembershipServiceIntegrationTest {

	@Autowired
	private ApproveMembershipUseCase approveMembershipUseCase;

	@Autowired
	private RejectMembershipUseCase rejectMembershipUseCase;

	@Autowired
	private SaveMembershipPort saveMembershipPort;

	@Autowired
	private LoadMembershipPort loadMembershipPort;

	@Test
	void approvePersistsAdminUuidAndApprovedStatus() {
		MembershipUuid membershipUuid = MembershipUuid.from("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
		AdminUuid adminUuid = AdminUuid.from("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
		saveMembershipPort.save(pending(membershipUuid, "11111111-1111-1111-1111-111111111111"));

		approveMembershipUseCase.approve(new ApproveMembershipCommand(membershipUuid, adminUuid));
		Membership saved = loadMembershipPort.findByUuid(membershipUuid).orElseThrow();

		assertThat(saved.status()).isEqualTo(MembershipStatus.APPROVED);
		assertThat(saved.adminUuid()).isEqualTo(adminUuid);
		assertThat(saved.rejectReason()).isNull();
	}

	@Test
	void rejectPersistsAdminUuidAndRejectReason() {
		MembershipUuid membershipUuid = MembershipUuid.from("cccccccc-cccc-cccc-cccc-cccccccccccc");
		AdminUuid adminUuid = AdminUuid.from("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
		saveMembershipPort.save(pending(membershipUuid, "33333333-3333-3333-3333-333333333333"));

		rejectMembershipUseCase.reject(new RejectMembershipCommand(membershipUuid, adminUuid, "서류 미비"));
		Membership saved = loadMembershipPort.findByUuid(membershipUuid).orElseThrow();

		assertThat(saved.status()).isEqualTo(MembershipStatus.REJECTED);
		assertThat(saved.adminUuid()).isEqualTo(adminUuid);
		assertThat(saved.rejectReason()).isEqualTo("서류 미비");
	}

	private static Membership pending(MembershipUuid membershipUuid, String creatorUuid) {
		return Membership.apply(
				membershipUuid,
				CreatorUuid.from(creatorUuid),
				"윤휘명의 여행 멤버십",
				"월간 멤버십",
				100,
				Instant.parse("2026-08-22T00:00:00Z")
		);
	}
}
