package com.planwith.planwith_fo_membership.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_membership.adapter.out.grade.InMemoryGradeQueryAdapter;
import com.planwith.planwith_fo_membership.adapter.out.persistence.membership.InMemoryLoadMembershipPort;
import com.planwith.planwith_fo_membership.application.command.ValidateMembershipApplicationCommand;
import com.planwith.planwith_fo_membership.application.query.MemberGradeResult;
import com.planwith.planwith_fo_membership.application.query.ValidateMembershipApplicationResult;
import com.planwith.planwith_fo_membership.domain.exception.DuplicateMembershipApplicationException;
import com.planwith.planwith_fo_membership.domain.exception.InsufficientMembershipGradeException;
import com.planwith.planwith_fo_membership.domain.exception.InvalidMembershipPriceException;
import com.planwith.planwith_fo_membership.domain.model.Membership;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;
import com.planwith.planwith_fo_membership.domain.service.MembershipApplicationPolicy;

class ValidateMembershipApplicationServiceTest {

	private final CreatorUuid creatorUuid = CreatorUuid.from("22222222-2222-2222-2222-222222222222");
	private final MemberUuid memberUuid = new MemberUuid(creatorUuid.value());

	private InMemoryGradeQueryAdapter gradeQueryPort;
	private InMemoryLoadMembershipPort loadMembershipPort;
	private ValidateMembershipApplicationService service;

	@BeforeEach
	void setUp() {
		gradeQueryPort = new InMemoryGradeQueryAdapter();
		loadMembershipPort = new InMemoryLoadMembershipPort();
		service = new ValidateMembershipApplicationService(gradeQueryPort, loadMembershipPort);
	}

	@Test
	void explorerCanApplyWithTokenPrice() {
		gradeQueryPort.put(new MemberGradeResult(memberUuid, "EXPLORER", "탐험가", 4));

		ValidateMembershipApplicationResult result = service.validate(validCommand());

		assertThat(result.eligible()).isTrue();
		assertThat(result.gradeCode()).isEqualTo("EXPLORER");
		assertThat(result.monthlyPrice()).isEqualTo(12900);
		assertThat(result.priceUnit()).isEqualTo(MembershipApplicationPolicy.PRICE_UNIT_TOKEN);
	}

	@Test
	void travelerCannotOpenMembership() {
		gradeQueryPort.put(new MemberGradeResult(memberUuid, "TRAVELER", "여행가", 3));

		assertThatThrownBy(() -> service.validate(validCommand()))
				.isInstanceOf(InsufficientMembershipGradeException.class);
	}

	@Test
	void rejectsNonTokenPriceUnit() {
		gradeQueryPort.put(new MemberGradeResult(memberUuid, "EXPLORER", "탐험가", 4));

		assertThatThrownBy(() -> service.validate(new ValidateMembershipApplicationCommand(
				creatorUuid,
				"크리에이터 멤버십",
				"월간 멤버십",
				12900,
				"KRW"
		))).isInstanceOf(InvalidMembershipPriceException.class);
	}

	@Test
	void rejectsNonPositivePrice() {
		gradeQueryPort.put(new MemberGradeResult(memberUuid, "EXPLORER", "탐험가", 4));

		assertThatThrownBy(() -> service.validate(new ValidateMembershipApplicationCommand(
				creatorUuid,
				"크리에이터 멤버십",
				"월간 멤버십",
				0,
				"TOKEN"
		))).isInstanceOf(InvalidMembershipPriceException.class);
	}

	@Test
	void rejectsDuplicatePendingApplication() {
		gradeQueryPort.put(new MemberGradeResult(memberUuid, "EXPLORER", "탐험가", 4));
		loadMembershipPort.save(Membership.apply(
				new MembershipUuid(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")),
				creatorUuid,
				"기존 신청",
				"설명",
				9900,
				Instant.parse("2026-08-22T00:00:00Z")
		));

		assertThatThrownBy(() -> service.validate(validCommand()))
				.isInstanceOf(DuplicateMembershipApplicationException.class);
	}

	private ValidateMembershipApplicationCommand validCommand() {
		return new ValidateMembershipApplicationCommand(
				creatorUuid,
				"크리에이터 멤버십",
				"월간 멤버십",
				12900,
				"TOKEN"
		);
	}
}
