package com.planwith.planwith_fo_membership.adapter.out.persistence.outbox;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_membership.application.port.out.MembershipEventOutboxPort;
import com.planwith.planwith_fo_membership.application.port.out.MembershipOutboxMessage;

@Component
public class MembershipEventOutboxAdapter implements MembershipEventOutboxPort {

	private static final Logger log = LoggerFactory.getLogger(MembershipEventOutboxAdapter.class);

	private final SpringDataMembershipOutboxRepository repository;

	public MembershipEventOutboxAdapter(SpringDataMembershipOutboxRepository repository) {
		this.repository = repository;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void save(MembershipOutboxMessage message) {
		UUID eventUuid = UUID.fromString(message.eventUuid());
		if (repository.existsByEventUuid(eventUuid)) {
			log.warn("MembershipEventOutboxAdapter : save : 중복 Outbox 이벤트 저장 생략 - eventUuid={}",
					message.eventUuid());
			return;
		}
		repository.save(new MembershipOutboxJpaEntity(
				eventUuid,
				message.aggregateType(),
				UUID.fromString(message.aggregateUuid()),
				message.eventType(),
				message.payload(),
				message.occurredAt()
		));
		log.info("MembershipEventOutboxAdapter : save : 멤버십 Outbox 저장 완료 - eventUuid={}, eventType={}",
				message.eventUuid(), message.eventType());
	}
}
