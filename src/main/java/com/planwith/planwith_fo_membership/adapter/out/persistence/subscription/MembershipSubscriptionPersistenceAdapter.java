package com.planwith.planwith_fo_membership.adapter.out.persistence.subscription;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_membership.application.port.out.LoadSubscriptionPort;
import com.planwith.planwith_fo_membership.application.port.out.SaveSubscriptionPort;
import com.planwith.planwith_fo_membership.application.query.JoinedMembershipResult;
import com.planwith.planwith_fo_membership.domain.exception.DuplicateSubscriptionException;
import com.planwith.planwith_fo_membership.domain.model.MembershipSubscription;
import com.planwith.planwith_fo_membership.domain.model.SubscriptionStatus;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;
import com.planwith.planwith_fo_membership.domain.service.MembershipApplicationPolicy;
import com.planwith.planwith_fo_membership.domain.service.SubscriptionPolicy;

@Component
public class MembershipSubscriptionPersistenceAdapter implements LoadSubscriptionPort, SaveSubscriptionPort {

	private static final Logger log = LoggerFactory.getLogger(MembershipSubscriptionPersistenceAdapter.class);

	private final SpringDataMembershipSubscriptionRepository repository;

	public MembershipSubscriptionPersistenceAdapter(SpringDataMembershipSubscriptionRepository repository) {
		this.repository = repository;
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<MembershipSubscription> findByUuid(SubscriptionUuid subscriptionUuid) {
		return repository.findBySubscriptionUuid(subscriptionUuid.value())
				.map(MembershipSubscriptionJpaEntity::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<MembershipSubscription> findCurrentByMemberAndCreator(MemberUuid memberUuid, CreatorUuid creatorUuid) {
		return repository.findCurrentByMemberAndCreator(
						memberUuid.value(),
						creatorUuid.value(),
						SubscriptionStatus.ACTIVE
				)
				.map(MembershipSubscriptionJpaEntity::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public List<JoinedMembershipResult> findJoinedByMember(MemberUuid memberUuid) {
		return repository.findJoinedByMemberUuid(memberUuid.value(), SubscriptionStatus.ACTIVE)
				.stream()
				.map(MembershipSubscriptionPersistenceAdapter::toJoinedResult)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<MembershipSubscription> findActiveByCreator(CreatorUuid creatorUuid) {
		return repository.findActiveByCreatorUuid(creatorUuid.value(), SubscriptionStatus.ACTIVE)
				.stream()
				.map(MembershipSubscriptionJpaEntity::toDomain)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<MembershipSubscription> findActiveStartedAtOnOrBefore(Instant startedAtOnOrBefore) {
		return repository.findByStatusAndStartedAtLessThanEqual(SubscriptionStatus.ACTIVE, startedAtOnOrBefore)
				.stream()
				.map(MembershipSubscriptionJpaEntity::toDomain)
				.toList();
	}

	@Override
	@Transactional
	public void save(MembershipSubscription subscription) {
		if (subscription.status() == SubscriptionStatus.ACTIVE) {
			boolean hasOtherActive = repository
					.findFirstByMemberUuidAndMembershipUuidAndStatusOrderByStartedAtDesc(
							subscription.memberUuid().value(),
							subscription.membershipUuid().value(),
							SubscriptionStatus.ACTIVE
					)
					.filter(existing -> !existing.subscriptionUuid().equals(subscription.subscriptionUuid().value()))
					.isPresent();
			if (SubscriptionPolicy.isDuplicateActive(hasOtherActive)) {
				log.warn(
						"MembershipSubscriptionPersistenceAdapter : save : 동일 플랜 활성 구독이 있어 저장하지 않는다 - memberUuid={}, membershipUuid={}",
						subscription.memberUuid(),
						subscription.membershipUuid()
				);
				throw new DuplicateSubscriptionException("이미 가입한 멤버십입니다.");
			}
		}
		MembershipSubscriptionJpaEntity entity = repository
				.findBySubscriptionUuid(subscription.subscriptionUuid().value())
				.orElseGet(() -> MembershipSubscriptionJpaEntity.from(subscription));
		entity.apply(subscription);
		repository.save(entity);
	}

	private static JoinedMembershipResult toJoinedResult(JoinedMembershipRow row) {
		return new JoinedMembershipResult(
				new SubscriptionUuid(row.getSubscriptionUuid()),
				new MembershipUuid(row.getMembershipUuid()),
				new CreatorUuid(row.getCreatorUuid()),
				row.getMembershipName(),
				row.getMonthlyPrice(),
				MembershipApplicationPolicy.PRICE_UNIT_TOKEN,
				row.getStatus(),
				row.getStartedAt(),
				row.getEndedAt()
		);
	}
}
