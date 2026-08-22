package com.planwith.planwith_fo_membership.adapter.out.persistence.saga;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_membership.application.port.out.SaveMembershipSagaPort;
import com.planwith.planwith_fo_membership.domain.model.MembershipSaga;

@Component
public class MembershipSagaPersistenceAdapter implements SaveMembershipSagaPort {

	private final SpringDataMembershipSagaRepository repository;

	public MembershipSagaPersistenceAdapter(SpringDataMembershipSagaRepository repository) {
		this.repository = repository;
	}

	@Override
	@Transactional
	public void save(MembershipSaga saga) {
		MembershipSagaJpaEntity entity = repository.findBySagaUuid(saga.sagaUuid())
				.orElseGet(() -> MembershipSagaJpaEntity.from(saga));
		entity.apply(saga);
		repository.save(entity);
	}
}
