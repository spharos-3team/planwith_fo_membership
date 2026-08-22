package com.planwith.planwith_fo_membership.adapter.out.persistence.saga;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.planwith.planwith_fo_membership.application.port.out.LoadMembershipSagaPort;
import com.planwith.planwith_fo_membership.application.port.out.SaveMembershipSagaPort;
import com.planwith.planwith_fo_membership.domain.model.MembershipSaga;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.PaymentUuid;

public class InMemoryMembershipSagaPort implements LoadMembershipSagaPort, SaveMembershipSagaPort {

	private final Map<UUID, MembershipSaga> sagas = new HashMap<>();

	@Override
	public void save(MembershipSaga saga) {
		sagas.put(saga.sagaUuid(), saga);
	}

	@Override
	public Optional<MembershipSaga> findInProgressByMemberAndCreator(MemberUuid memberUuid, CreatorUuid creatorUuid) {
		return sagas.values().stream()
				.filter(saga -> saga.memberUuid().equals(memberUuid))
				.filter(saga -> saga.creatorUuid().equals(creatorUuid))
				.filter(MembershipSaga::isPaymentInProgress)
				.findFirst();
	}

	@Override
	public Optional<MembershipSaga> findByPaymentUuid(PaymentUuid paymentUuid) {
		return sagas.values().stream()
				.filter(saga -> paymentUuid.equals(saga.paymentUuid()))
				.findFirst();
	}
}
