package com.planwith.planwith_fo_membership.adapter.out.persistence.saga;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_membership.application.port.out.LoadMembershipSagaPort;
import com.planwith.planwith_fo_membership.application.port.out.SaveMembershipSagaPort;
import com.planwith.planwith_fo_membership.domain.model.MembershipSaga;
import com.planwith.planwith_fo_membership.domain.model.MembershipSagaStatus;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;

@Component
public class MembershipSagaPersistenceAdapter implements LoadMembershipSagaPort, SaveMembershipSagaPort {

	private static final List<MembershipSagaStatus> IN_PROGRESS = List.of(
			MembershipSagaStatus.SUBSCRIBE_REQUESTED,
			MembershipSagaStatus.PAYMENT_PENDING
	);

	private final SpringDataMembershipSagaRepository repository;

	public MembershipSagaPersistenceAdapter(SpringDataMembershipSagaRepository repository) {
		this.repository = repository;
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<MembershipSaga> findInProgressByMemberAndCreator(MemberUuid memberUuid, CreatorUuid creatorUuid) {
		return repository.findFirstByMemberUuidAndCreatorUuidAndStatusInOrderByUpdatedAtDesc(
						memberUuid.value(),
						creatorUuid.value(),
						IN_PROGRESS
				)
				.map(MembershipSagaJpaEntity::toDomain);
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
