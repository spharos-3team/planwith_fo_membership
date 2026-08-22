package com.planwith.planwith_fo_membership.adapter.out.token;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_membership.application.command.TokenDeductionCommand;
import com.planwith.planwith_fo_membership.application.port.out.TokenCommandPort;
import com.planwith.planwith_fo_membership.application.query.TokenDeductionResult;
import com.planwith.planwith_fo_membership.domain.exception.UnsupportedMembershipOperationException;

@Component
public class StubTokenCommandAdapter implements TokenCommandPort {

	private static final Logger log = LoggerFactory.getLogger(StubTokenCommandAdapter.class);

	@Override
	public TokenDeductionResult requestTokenDeduction(TokenDeductionCommand command) {
		log.debug(
				"StubTokenCommandAdapter : requestTokenDeduction : Token 서비스 차감 요청은 후속 이슈에서 구현한다 - memberUuid={}, amount={}, referenceType={}",
				command.memberUuid(),
				command.amount(),
				command.referenceType()
		);
		throw new UnsupportedMembershipOperationException("Token 서비스 토큰 차감 요청은 후속 이슈에서 구현한다.");
	}
}
