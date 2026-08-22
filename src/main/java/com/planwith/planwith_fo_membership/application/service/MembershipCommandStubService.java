package com.planwith.planwith_fo_membership.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.planwith.planwith_fo_membership.application.command.CancelSubscriptionCommand;
import com.planwith.planwith_fo_membership.application.command.CreateMembershipCommand;
import com.planwith.planwith_fo_membership.application.command.ExpireSubscriptionCommand;
import com.planwith.planwith_fo_membership.application.command.ProcessSettlementCommand;
import com.planwith.planwith_fo_membership.application.command.RequestSettlementCommand;
import com.planwith.planwith_fo_membership.application.command.SubscribeMembershipCommand;
import com.planwith.planwith_fo_membership.application.command.UpdateMembershipCommand;
import com.planwith.planwith_fo_membership.application.port.in.command.CancelSubscriptionUseCase;
import com.planwith.planwith_fo_membership.application.port.in.command.CreateMembershipUseCase;
import com.planwith.planwith_fo_membership.application.port.in.command.ExpireSubscriptionUseCase;
import com.planwith.planwith_fo_membership.application.port.in.command.ProcessSettlementUseCase;
import com.planwith.planwith_fo_membership.application.port.in.command.RequestSettlementUseCase;
import com.planwith.planwith_fo_membership.application.port.in.command.SubscribeMembershipUseCase;
import com.planwith.planwith_fo_membership.application.port.in.command.UpdateMembershipUseCase;
import com.planwith.planwith_fo_membership.domain.exception.UnsupportedMembershipOperationException;

@Service
public class MembershipCommandStubService implements
		CreateMembershipUseCase,
		UpdateMembershipUseCase,
		SubscribeMembershipUseCase,
		CancelSubscriptionUseCase,
		ExpireSubscriptionUseCase,
		RequestSettlementUseCase,
		ProcessSettlementUseCase {

	private static final Logger log = LoggerFactory.getLogger(MembershipCommandStubService.class);

	@Override
	public void create(CreateMembershipCommand command) {
		log.debug("MembershipCommandStubService : create : 멤버십 생성 정책은 후속 이슈에서 구현한다");
		throw unsupported("멤버십 생성");
	}

	@Override
	public void update(UpdateMembershipCommand command) {
		log.debug("MembershipCommandStubService : update : 멤버십 수정 정책은 후속 이슈에서 구현한다");
		throw unsupported("멤버십 수정");
	}

	@Override
	public void subscribe(SubscribeMembershipCommand command) {
		log.debug(
				"MembershipCommandStubService : subscribe : 가입 Saga는 Payment와 로컬 트랜잭션을 분리하며 후속 이슈에서 구현한다 - memberUuid={}",
				command.memberUuid()
		);
		throw unsupported("멤버십 구독 Saga");
	}

	@Override
	public void cancel(CancelSubscriptionCommand command) {
		log.debug("MembershipCommandStubService : cancel : 구독 해지는 후속 이슈에서 구현한다 - subscriptionUuid={}",
				command.subscriptionUuid());
		throw unsupported("구독 해지");
	}

	@Override
	public void expire(ExpireSubscriptionCommand command) {
		log.debug("MembershipCommandStubService : expire : 구독 만료는 후속 이슈에서 구현한다 - subscriptionUuid={}",
				command.subscriptionUuid());
		throw unsupported("구독 만료");
	}

	@Override
	public void request(RequestSettlementCommand command) {
		log.debug("MembershipCommandStubService : request : 정산 요청은 후속 이슈에서 구현한다 - creatorUuid={}",
				command.creatorUuid());
		throw unsupported("정산 요청");
	}

	@Override
	public void process(ProcessSettlementCommand command) {
		log.debug("MembershipCommandStubService : process : 정산 처리는 후속 이슈에서 구현한다 - settlementUuid={}",
				command.settlementUuid());
		throw unsupported("정산 처리");
	}

	private static UnsupportedMembershipOperationException unsupported(String operation) {
		return new UnsupportedMembershipOperationException(operation + "은 후속 이슈에서 구현한다.");
	}
}
