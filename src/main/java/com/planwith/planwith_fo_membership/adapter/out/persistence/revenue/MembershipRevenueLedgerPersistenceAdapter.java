package com.planwith.planwith_fo_membership.adapter.out.persistence.revenue;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_membership.application.port.out.LoadRevenueLedgerPort;
import com.planwith.planwith_fo_membership.application.port.out.SaveRevenueLedgerPort;
import com.planwith.planwith_fo_membership.domain.model.MembershipRevenueLedger;
import com.planwith.planwith_fo_membership.domain.model.vo.PaymentUuid;

@Component
public class MembershipRevenueLedgerPersistenceAdapter implements LoadRevenueLedgerPort, SaveRevenueLedgerPort {

	private static final Logger log = LoggerFactory.getLogger(MembershipRevenueLedgerPersistenceAdapter.class);

	private final SpringDataMembershipRevenueLedgerRepository repository;

	public MembershipRevenueLedgerPersistenceAdapter(SpringDataMembershipRevenueLedgerRepository repository) {
		this.repository = repository;
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<MembershipRevenueLedger> findByPaymentUuid(PaymentUuid paymentUuid) {
		return repository.findByPaymentUuid(paymentUuid.value())
				.map(MembershipRevenueLedgerJpaEntity::toDomain);
	}

	@Override
	@Transactional
	public void save(MembershipRevenueLedger ledger) {
		if (repository.findByPaymentUuid(ledger.paymentUuid().value()).isPresent()) {
			log.warn(
					"MembershipRevenueLedgerPersistenceAdapter : save : 동일 결제 수익 원장이 있어 신규 기록을 생략한다 - paymentUuid={}",
					ledger.paymentUuid()
			);
			return;
		}
		repository.save(MembershipRevenueLedgerJpaEntity.from(ledger));
		log.debug(
				"MembershipRevenueLedgerPersistenceAdapter : save : 수익 원장 기록 - paymentUuid={}, creatorShareKrw={}",
				ledger.paymentUuid(),
				ledger.creatorShareKrw()
		);
	}
}
