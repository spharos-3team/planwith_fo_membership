package com.planwith.planwith_fo_membership.adapter.out.persistence.settlement;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_membership.application.port.out.LoadSettlementPort;
import com.planwith.planwith_fo_membership.application.port.out.SaveSettlementPort;
import com.planwith.planwith_fo_membership.domain.model.MembershipSettlement;
import com.planwith.planwith_fo_membership.domain.model.vo.SettlementUuid;

@Component
public class MembershipSettlementPersistenceAdapter implements LoadSettlementPort, SaveSettlementPort {

	private static final Logger log = LoggerFactory.getLogger(MembershipSettlementPersistenceAdapter.class);

	private final SpringDataMembershipSettlementRepository repository;

	public MembershipSettlementPersistenceAdapter(SpringDataMembershipSettlementRepository repository) {
		this.repository = repository;
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<MembershipSettlement> findByUuid(SettlementUuid settlementUuid) {
		return repository.findBySettlementUuid(settlementUuid.value())
				.map(MembershipSettlementJpaEntity::toDomain);
	}

	@Override
	@Transactional
	public void save(MembershipSettlement settlement) {
		MembershipSettlementJpaEntity entity = repository.findBySettlementUuid(settlement.settlementUuid().value())
				.orElseGet(() -> MembershipSettlementJpaEntity.from(settlement));
		entity.apply(settlement);
		repository.save(entity);
		log.debug(
				"MembershipSettlementPersistenceAdapter : save : 멤버십 정산 저장 - settlementUuid={}, status={}",
				settlement.settlementUuid(),
				settlement.settlementStatus()
		);
	}
}
