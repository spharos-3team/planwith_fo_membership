package com.planwith.planwith_fo_membership.adapter.out.persistence.processed;

import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_membership.application.port.out.ProcessedMembershipEventPort;
import com.planwith.planwith_fo_membership.domain.model.ProcessedMembershipEvent;

@Component
public class ProcessedMembershipEventPersistenceAdapter implements ProcessedMembershipEventPort {

	private final SpringDataProcessedMembershipEventRepository repository;

	public ProcessedMembershipEventPersistenceAdapter(SpringDataProcessedMembershipEventRepository repository) {
		this.repository = repository;
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsByEventUuid(UUID eventUuid) {
		return repository.existsByEventUuid(eventUuid);
	}

	@Override
	@Transactional
	public void save(ProcessedMembershipEvent event) {
		repository.save(toEntity(event));
	}

	@Override
	@Transactional
	public boolean saveIdempotent(ProcessedMembershipEvent event) {
		if (existsByEventUuid(event.eventUuid())) {
			return false;
		}
		try {
			repository.save(toEntity(event));
			return true;
		} catch (DataIntegrityViolationException exception) {
			return false;
		}
	}

	private static ProcessedMembershipEventJpaEntity toEntity(ProcessedMembershipEvent event) {
		return ProcessedMembershipEventJpaEntity.create(
				event.eventUuid(),
				event.memberUuid().value(),
				event.eventType(),
				event.processedAt()
		);
	}
}
