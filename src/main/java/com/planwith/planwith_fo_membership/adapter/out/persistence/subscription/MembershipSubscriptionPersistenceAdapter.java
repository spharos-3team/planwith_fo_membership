package com.planwith.planwith_fo_membership.adapter.out.persistence.subscription;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_membership.application.port.out.LoadSubscriptionPort;
import com.planwith.planwith_fo_membership.application.port.out.SaveSubscriptionPort;
import com.planwith.planwith_fo_membership.domain.model.Subscription;
import com.planwith.planwith_fo_membership.domain.model.SubscriptionStatus;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;

@Component
public class MembershipSubscriptionPersistenceAdapter implements LoadSubscriptionPort, SaveSubscriptionPort {

	private final SpringDataMembershipSubscriptionRepository repository;

	public MembershipSubscriptionPersistenceAdapter(SpringDataMembershipSubscriptionRepository repository) {
		this.repository = repository;
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Subscription> findByUuid(SubscriptionUuid subscriptionUuid) {
		return repository.findBySubscriptionUuid(subscriptionUuid.value())
				.map(MembershipSubscriptionJpaEntity::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Subscription> findCurrentByMemberAndCreator(MemberUuid memberUuid, CreatorUuid creatorUuid) {
		return repository.findCurrentByMemberAndCreator(
						memberUuid.value(),
						creatorUuid.value(),
						SubscriptionStatus.ACTIVE
				)
				.map(MembershipSubscriptionJpaEntity::toDomain);
	}

	@Override
	@Transactional
	public void save(Subscription subscription) {
		MembershipSubscriptionJpaEntity entity = repository
				.findBySubscriptionUuid(subscription.subscriptionUuid().value())
				.orElseGet(() -> MembershipSubscriptionJpaEntity.from(subscription));
		entity.apply(subscription);
		repository.save(entity);
	}
}
