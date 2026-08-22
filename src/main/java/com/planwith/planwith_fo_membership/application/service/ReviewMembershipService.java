package com.planwith.planwith_fo_membership.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_membership.application.command.ApproveMembershipCommand;
import com.planwith.planwith_fo_membership.application.command.RejectMembershipCommand;
import com.planwith.planwith_fo_membership.application.port.in.command.ApproveMembershipUseCase;
import com.planwith.planwith_fo_membership.application.port.in.command.RejectMembershipUseCase;
import com.planwith.planwith_fo_membership.application.port.out.LoadMembershipPort;
import com.planwith.planwith_fo_membership.application.port.out.SaveMembershipPort;
import com.planwith.planwith_fo_membership.application.query.ReviewMembershipResult;
import com.planwith.planwith_fo_membership.domain.exception.InvalidMembershipStateException;
import com.planwith.planwith_fo_membership.domain.exception.MembershipNotFoundException;
import com.planwith.planwith_fo_membership.domain.model.Membership;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;

@Service
public class ReviewMembershipService implements ApproveMembershipUseCase, RejectMembershipUseCase {

	private static final Logger log = LoggerFactory.getLogger(ReviewMembershipService.class);

	private final LoadMembershipPort loadMembershipPort;
	private final SaveMembershipPort saveMembershipPort;

	public ReviewMembershipService(
			LoadMembershipPort loadMembershipPort,
			SaveMembershipPort saveMembershipPort
	) {
		this.loadMembershipPort = loadMembershipPort;
		this.saveMembershipPort = saveMembershipPort;
	}

	@Override
	@Transactional
	public ReviewMembershipResult approve(ApproveMembershipCommand command) {
		log.info(
				"ReviewMembershipService : approve : 멤버십 승인 요청 - membershipUuid={}, adminUuid={}",
				command.membershipUuid(),
				command.adminUuid()
		);
		Membership membership = requireMembership(command.membershipUuid());
		try {
			Membership approved = membership.approve(command.adminUuid());
			saveMembershipPort.save(approved);
			log.info(
					"ReviewMembershipService : approve : 멤버십 승인 완료 - membershipUuid={}, status={}",
					approved.membershipUuid(),
					approved.status()
			);
			return toResult(approved);
		} catch (InvalidMembershipStateException exception) {
			log.warn(
					"ReviewMembershipService : approve : 잘못된 멤버십 승인 요청 - membershipUuid={}, status={}",
					membership.membershipUuid(),
					membership.status()
			);
			throw exception;
		}
	}

	@Override
	@Transactional
	public ReviewMembershipResult reject(RejectMembershipCommand command) {
		log.info(
				"ReviewMembershipService : reject : 멤버십 거절 요청 - membershipUuid={}, adminUuid={}",
				command.membershipUuid(),
				command.adminUuid()
		);
		Membership membership = requireMembership(command.membershipUuid());
		try {
			Membership rejected = membership.reject(command.adminUuid(), command.rejectReason());
			saveMembershipPort.save(rejected);
			log.info(
					"ReviewMembershipService : reject : 멤버십 거절 완료 - membershipUuid={}, status={}",
					rejected.membershipUuid(),
					rejected.status()
			);
			return toResult(rejected);
		} catch (InvalidMembershipStateException exception) {
			log.warn(
					"ReviewMembershipService : reject : 잘못된 멤버십 거절 요청 - membershipUuid={}, status={}",
					membership.membershipUuid(),
					membership.status()
			);
			throw exception;
		}
	}

	private Membership requireMembership(MembershipUuid membershipUuid) {
		return loadMembershipPort.findByUuid(membershipUuid)
				.orElseThrow(() -> new MembershipNotFoundException("멤버십을 찾을 수 없습니다."));
	}

	private static ReviewMembershipResult toResult(Membership membership) {
		return new ReviewMembershipResult(
				membership.membershipUuid(),
				membership.creatorUuid(),
				membership.status(),
				membership.adminUuid(),
				membership.rejectReason()
		);
	}
}
