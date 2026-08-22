package com.planwith.planwith_fo_membership.adapter.out.persistence.membership;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_membership.application.port.out.LoadMembershipPort;
import com.planwith.planwith_fo_membership.application.port.out.SaveMembershipPort;
import com.planwith.planwith_fo_membership.domain.model.Membership;
import com.planwith.planwith_fo_membership.domain.model.MembershipStatus;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;

@Component
public class MembershipPersistenceAdapter implements LoadMembershipPort, SaveMembershipPort {

	private static final Logger log = LoggerFactory.getLogger(MembershipPersistenceAdapter.class);

	private final SpringDataMembershipRepository repository;

	public MembershipPersistenceAdapter(SpringDataMembershipRepository repository) {
		this.repository = repository;
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Membership> findByUuid(MembershipUuid membershipUuid) {
		return repository.findByMembershipUuid(membershipUuid.value())
				.map(MembershipJpaEntity::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Membership> findLatestByCreator(CreatorUuid creatorUuid) {
		return repository.findFirstByCreatorUuidOrderByCreateAtDesc(creatorUuid.value())
				.map(MembershipJpaEntity::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Membership> findOpenByCreator(CreatorUuid creatorUuid) {
		return repository.findFirstByCreatorUuidAndStatusIn(
						creatorUuid.value(),
						List.of(MembershipStatus.PENDING.name(), MembershipStatus.APPROVED.name())
				)
				.map(MembershipJpaEntity::toDomain);
	}

	@Override
	@Transactional
	public void save(Membership membership) {
		MembershipJpaEntity entity = repository.findByMembershipUuid(membership.membershipUuid().value())
				.orElseGet(() -> MembershipJpaEntity.from(membership));
		entity.apply(membership);
		repository.save(entity);
		log.debug(
				"MembershipPersistenceAdapter : save : 멤버십 저장 - membershipUuid={}, status={}",
				membership.membershipUuid(),
				membership.status()
		);
	}
}
