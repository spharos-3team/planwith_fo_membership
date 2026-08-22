package com.planwith.planwith_fo_membership.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_membership.application.command.ValidateMembershipApplicationCommand;
import com.planwith.planwith_fo_membership.application.port.in.command.ValidateMembershipApplicationUseCase;
import com.planwith.planwith_fo_membership.application.port.out.GradeQueryPort;
import com.planwith.planwith_fo_membership.application.port.out.LoadMembershipPort;
import com.planwith.planwith_fo_membership.application.query.MemberGradeResult;
import com.planwith.planwith_fo_membership.application.query.ValidateMembershipApplicationResult;
import com.planwith.planwith_fo_membership.domain.exception.DuplicateMembershipApplicationException;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.service.MembershipApplicationPolicy;

@Service
public class ValidateMembershipApplicationService implements ValidateMembershipApplicationUseCase {

	private static final Logger log = LoggerFactory.getLogger(ValidateMembershipApplicationService.class);

	private final GradeQueryPort gradeQueryPort;
	private final LoadMembershipPort loadMembershipPort;

	public ValidateMembershipApplicationService(
			GradeQueryPort gradeQueryPort,
			LoadMembershipPort loadMembershipPort
	) {
		this.gradeQueryPort = gradeQueryPort;
		this.loadMembershipPort = loadMembershipPort;
	}

	@Override
	@Transactional(readOnly = true)
	public ValidateMembershipApplicationResult validate(ValidateMembershipApplicationCommand command) {
		log.info(
				"ValidateMembershipApplicationService : validate : 멤버십 신청 검증 시작 - creatorUuid={}",
				command.creatorUuid()
		);
		validatePrice(command);
		MemberGradeResult grade = gradeQueryPort.getMemberGrade(new MemberUuid(command.creatorUuid().value()));
		log.debug(
				"ValidateMembershipApplicationService : validate : 신청 등급 확인 - creatorUuid={}, gradeCode={}, gradeLevel={}",
				command.creatorUuid(),
				grade.gradeCode(),
				grade.gradeLevel()
		);
		MembershipApplicationPolicy.requireEligibleGrade(grade.gradeCode(), grade.gradeLevel());
		if (loadMembershipPort.findOpenByCreator(command.creatorUuid())
				.filter(MembershipApplicationPolicy::isDuplicateApplication)
				.isPresent()) {
			log.warn(
					"ValidateMembershipApplicationService : validate : 중복 멤버십 신청 - creatorUuid={}",
					command.creatorUuid()
			);
			throw new DuplicateMembershipApplicationException("이미 진행 중인 멤버십 신청 또는 운영 중인 멤버십이 있습니다.");
		}
		log.info(
				"ValidateMembershipApplicationService : validate : 멤버십 신청 검증 완료 - creatorUuid={}, gradeCode={}",
				command.creatorUuid(),
				grade.gradeCode()
		);
		return new ValidateMembershipApplicationResult(
				command.creatorUuid(),
				grade.gradeCode(),
				command.monthlyPrice(),
				MembershipApplicationPolicy.PRICE_UNIT_TOKEN,
				true
		);
	}

	private static void validatePrice(ValidateMembershipApplicationCommand command) {
		MembershipApplicationPolicy.requireValidPrice(command.monthlyPrice(), command.priceUnit());
	}
}
