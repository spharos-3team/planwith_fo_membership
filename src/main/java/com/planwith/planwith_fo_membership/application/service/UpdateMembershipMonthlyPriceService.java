package com.planwith.planwith_fo_membership.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_membership.application.command.UpdateMembershipCommand;
import com.planwith.planwith_fo_membership.application.port.in.command.UpdateMembershipUseCase;
import com.planwith.planwith_fo_membership.application.port.out.LoadMembershipPort;
import com.planwith.planwith_fo_membership.application.port.out.SaveMembershipPort;
import com.planwith.planwith_fo_membership.application.query.UpdateMembershipMonthlyPriceResult;
import com.planwith.planwith_fo_membership.domain.exception.ForbiddenCreatorException;
import com.planwith.planwith_fo_membership.domain.exception.InvalidMembershipStateException;
import com.planwith.planwith_fo_membership.domain.exception.MembershipNotFoundException;
import com.planwith.planwith_fo_membership.domain.model.Membership;
import com.planwith.planwith_fo_membership.domain.service.MembershipApplicationPolicy;

@Service
public class UpdateMembershipMonthlyPriceService implements UpdateMembershipUseCase {

	private static final Logger log = LoggerFactory.getLogger(UpdateMembershipMonthlyPriceService.class);

	private final LoadMembershipPort loadMembershipPort;
	private final SaveMembershipPort saveMembershipPort;

	public UpdateMembershipMonthlyPriceService(
			LoadMembershipPort loadMembershipPort,
			SaveMembershipPort saveMembershipPort
	) {
		this.loadMembershipPort = loadMembershipPort;
		this.saveMembershipPort = saveMembershipPort;
	}

	@Override
	@Transactional
	public UpdateMembershipMonthlyPriceResult update(UpdateMembershipCommand command) {
		log.info(
				"UpdateMembershipMonthlyPriceService : update : 월 구독 토큰 변경 시작 - creatorUuid={}, monthlyPrice={}",
				command.creatorUuid(),
				command.monthlyPrice()
		);
		MembershipApplicationPolicy.requireValidPrice(
				command.monthlyPrice(),
				MembershipApplicationPolicy.PRICE_UNIT_TOKEN
		);

		Membership membership = loadMembershipPort.findOpenByCreator(command.creatorUuid())
				.orElseThrow(() -> new MembershipNotFoundException("운영 중인 멤버십을 찾을 수 없습니다."));

		if (!membership.creatorUuid().equals(command.creatorUuid())) {
			throw new ForbiddenCreatorException("본인 멤버십만 수정할 수 있습니다.");
		}

		try {
			Membership updated = membership.changeMonthlyPrice(command.monthlyPrice());
			saveMembershipPort.save(updated);
			log.info(
					"UpdateMembershipMonthlyPriceService : update : 월 구독 토큰 변경 완료 - creatorUuid={}, membershipUuid={}, monthlyPrice={}",
					command.creatorUuid(),
					updated.membershipUuid(),
					updated.monthlyPrice()
			);
			return new UpdateMembershipMonthlyPriceResult(
					updated.membershipUuid(),
					updated.creatorUuid(),
					updated.membershipName(),
					updated.monthlyPrice(),
					MembershipApplicationPolicy.PRICE_UNIT_TOKEN,
					updated.status()
			);
		}
		catch (InvalidMembershipStateException exception) {
			log.warn(
					"UpdateMembershipMonthlyPriceService : update : 잘못된 월 구독 토큰 변경 요청 - creatorUuid={}, status={}",
					command.creatorUuid(),
					membership.status()
			);
			throw exception;
		}
	}
}
