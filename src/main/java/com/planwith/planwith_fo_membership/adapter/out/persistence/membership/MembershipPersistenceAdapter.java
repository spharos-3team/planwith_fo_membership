package com.planwith.planwith_fo_membership.adapter.out.persistence.membership;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_membership.application.port.out.SaveMembershipPort;
import com.planwith.planwith_fo_membership.domain.model.Membership;

@Component
public class MembershipPersistenceAdapter implements SaveMembershipPort {

	private final SpringDataMembershipRepository repository;

	public MembershipPersistenceAdapter(SpringDataMembershipRepository repository) {
		this.repository = repository;
	}

	@Override
	@Transactional
	public void save(Membership membership) {
		MembershipJpaEntity entity = repository.findByMembershipUuid(membership.membershipUuid().value())
				.orElseGet(() -> MembershipJpaEntity.from(membership));
		entity.apply(membership);
		repository.save(entity);
	}
}
