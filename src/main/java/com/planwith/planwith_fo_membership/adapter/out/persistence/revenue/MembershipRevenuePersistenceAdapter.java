package com.planwith.planwith_fo_membership.adapter.out.persistence.revenue;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_membership.application.port.out.LoadRevenuePort;
import com.planwith.planwith_fo_membership.application.port.out.SaveRevenuePort;
import com.planwith.planwith_fo_membership.domain.model.MembershipRevenue;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.RevenueUuid;

@Component
public class MembershipRevenuePersistenceAdapter implements LoadRevenuePort, SaveRevenuePort {

	private static final Logger log = LoggerFactory.getLogger(MembershipRevenuePersistenceAdapter.class);

	private final SpringDataMembershipRevenueRepository repository;

	public MembershipRevenuePersistenceAdapter(SpringDataMembershipRevenueRepository repository) {
		this.repository = repository;
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<MembershipRevenue> findByUuid(RevenueUuid revenueUuid) {
		return repository.findByRevenueUuid(revenueUuid.value())
				.map(MembershipRevenueJpaEntity::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<MembershipRevenue> findByCreator(CreatorUuid creatorUuid) {
		return repository.findByCreatorUuid(creatorUuid.value())
				.map(MembershipRevenueJpaEntity::toDomain);
	}

	@Override
	@Transactional
	public void save(MembershipRevenue revenue) {
		MembershipRevenueJpaEntity entity = repository.findByRevenueUuid(revenue.revenueUuid().value())
				.orElseGet(() -> MembershipRevenueJpaEntity.from(revenue));
		entity.apply(revenue);
		repository.save(entity);
		log.debug(
				"MembershipRevenuePersistenceAdapter : save : 멤버십 수익 저장 - revenueUuid={}, creatorUuid={}",
				revenue.revenueUuid(),
				revenue.creatorUuid()
		);
	}
}
