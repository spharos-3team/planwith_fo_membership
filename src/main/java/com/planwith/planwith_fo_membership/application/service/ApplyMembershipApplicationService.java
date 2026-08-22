package com.planwith.planwith_fo_membership.application.service;

import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_membership.application.command.ValidateMembershipApplicationCommand;
import com.planwith.planwith_fo_membership.application.port.in.command.ApplyMembershipApplicationUseCase;
import com.planwith.planwith_fo_membership.application.port.in.command.ValidateMembershipApplicationUseCase;
import com.planwith.planwith_fo_membership.application.port.out.LoadRevenuePort;
import com.planwith.planwith_fo_membership.application.port.out.SaveMembershipPort;
import com.planwith.planwith_fo_membership.application.port.out.SaveRevenuePort;
import com.planwith.planwith_fo_membership.application.query.ApplyMembershipApplicationResult;
import com.planwith.planwith_fo_membership.domain.model.Membership;
import com.planwith.planwith_fo_membership.domain.model.MembershipRevenue;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.RevenueUuid;
import com.planwith.planwith_fo_membership.domain.service.MembershipApplicationPolicy;

@Service
public class ApplyMembershipApplicationService implements ApplyMembershipApplicationUseCase {

	private static final Logger log = LoggerFactory.getLogger(ApplyMembershipApplicationService.class);

	private final ValidateMembershipApplicationUseCase validateMembershipApplicationUseCase;
	private final SaveMembershipPort saveMembershipPort;
	private final LoadRevenuePort loadRevenuePort;
	private final SaveRevenuePort saveRevenuePort;

	public ApplyMembershipApplicationService(
			ValidateMembershipApplicationUseCase validateMembershipApplicationUseCase,
			SaveMembershipPort saveMembershipPort,
			LoadRevenuePort loadRevenuePort,
			SaveRevenuePort saveRevenuePort
	) {
		this.validateMembershipApplicationUseCase = validateMembershipApplicationUseCase;
		this.saveMembershipPort = saveMembershipPort;
		this.loadRevenuePort = loadRevenuePort;
		this.saveRevenuePort = saveRevenuePort;
	}

	@Override
	@Transactional
	public ApplyMembershipApplicationResult apply(ValidateMembershipApplicationCommand command) {
		log.info(
				"ApplyMembershipApplicationService : apply : 멤버십 신청 시작 - creatorUuid={}",
				command.creatorUuid()
		);
		validateMembershipApplicationUseCase.validate(command);

		Membership membership = Membership.apply(
				new MembershipUuid(UUID.randomUUID()),
				command.creatorUuid(),
				command.membershipName(),
				command.description(),
				command.monthlyPrice(),
				Instant.now()
		);
		saveMembershipPort.save(membership);

		MembershipRevenue revenue = loadRevenuePort.findByCreator(command.creatorUuid())
				.orElseGet(() -> {
					MembershipRevenue created = MembershipRevenue.empty(
							new RevenueUuid(UUID.randomUUID()),
							command.creatorUuid()
					);
					saveRevenuePort.save(created);
					return created;
				});

		log.info(
				"ApplyMembershipApplicationService : apply : 멤버십 신청 완료 - creatorUuid={}, membershipUuid={}, revenueUuid={}, status={}",
				command.creatorUuid(),
				membership.membershipUuid(),
				revenue.revenueUuid(),
				membership.status()
		);
		return new ApplyMembershipApplicationResult(
				membership.membershipUuid(),
				membership.creatorUuid(),
				membership.membershipName(),
				membership.monthlyPrice(),
				MembershipApplicationPolicy.PRICE_UNIT_TOKEN,
				membership.status(),
				revenue.revenueUuid()
		);
	}
}
