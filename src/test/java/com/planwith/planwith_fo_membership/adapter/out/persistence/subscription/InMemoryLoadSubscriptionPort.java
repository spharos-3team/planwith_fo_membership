package com.planwith.planwith_fo_membership.adapter.out.persistence.subscription;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.planwith.planwith_fo_membership.application.port.out.LoadSubscriptionPort;
import com.planwith.planwith_fo_membership.application.port.out.SaveSubscriptionPort;
import com.planwith.planwith_fo_membership.application.query.JoinedMembershipResult;
import com.planwith.planwith_fo_membership.domain.exception.DuplicateSubscriptionException;
import com.planwith.planwith_fo_membership.domain.model.Membership;
import com.planwith.planwith_fo_membership.domain.model.MembershipSubscription;
import com.planwith.planwith_fo_membership.domain.model.SubscriptionStatus;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;
import com.planwith.planwith_fo_membership.domain.service.MembershipApplicationPolicy;
import com.planwith.planwith_fo_membership.domain.service.SubscriptionPolicy;

public class InMemoryLoadSubscriptionPort implements LoadSubscriptionPort, SaveSubscriptionPort {

	private final Map<SubscriptionUuid, MembershipSubscription> subscriptions = new HashMap<>();
	private final Map<MembershipUuid, Membership> memberships = new HashMap<>();

	public void registerMembership(Membership membership) {
		memberships.put(membership.membershipUuid(), membership);
	}

	@Override
	public void save(MembershipSubscription subscription) {
		if (subscription.status() == SubscriptionStatus.ACTIVE) {
			boolean hasOtherActive = subscriptions.values().stream()
					.filter(existing -> !existing.subscriptionUuid().equals(subscription.subscriptionUuid()))
					.filter(existing -> existing.memberUuid().equals(subscription.memberUuid()))
					.filter(existing -> existing.membershipUuid().equals(subscription.membershipUuid()))
					.anyMatch(existing -> existing.status() == SubscriptionStatus.ACTIVE);
			if (SubscriptionPolicy.isDuplicateActive(hasOtherActive)) {
				throw new DuplicateSubscriptionException("이미 가입한 멤버십입니다.");
			}
		}
		subscriptions.put(subscription.subscriptionUuid(), subscription);
	}

	@Override
	public Optional<MembershipSubscription> findByUuid(SubscriptionUuid subscriptionUuid) {
		return Optional.ofNullable(subscriptions.get(subscriptionUuid));
	}

	@Override
	public Optional<MembershipSubscription> findCurrentByMemberAndCreator(MemberUuid memberUuid, CreatorUuid creatorUuid) {
		return subscriptions.values().stream()
				.filter(subscription -> subscription.memberUuid().equals(memberUuid))
				.filter(subscription -> subscription.status() == SubscriptionStatus.ACTIVE)
				.filter(subscription -> matchesCreator(subscription, creatorUuid))
				.findFirst();
	}

	@Override
	public List<JoinedMembershipResult> findJoinedByMember(MemberUuid memberUuid) {
		return subscriptions.values().stream()
				.filter(subscription -> subscription.memberUuid().equals(memberUuid))
				.filter(subscription -> subscription.status() == SubscriptionStatus.ACTIVE)
				.map(this::toJoinedResult)
				.flatMap(Optional::stream)
				.toList();
	}

	@Override
	public List<MembershipSubscription> findActiveByCreator(CreatorUuid creatorUuid) {
		return subscriptions.values().stream()
				.filter(subscription -> subscription.status() == SubscriptionStatus.ACTIVE)
				.filter(subscription -> matchesCreator(subscription, creatorUuid))
				.toList();
	}

	@Override
	public List<MembershipSubscription> findActiveStartedAtOnOrBefore(Instant startedAtOnOrBefore) {
		return subscriptions.values().stream()
				.filter(subscription -> subscription.status() == SubscriptionStatus.ACTIVE)
				.filter(subscription -> !subscription.startedAt().isAfter(startedAtOnOrBefore))
				.toList();
	}

	private boolean matchesCreator(MembershipSubscription subscription, CreatorUuid creatorUuid) {
		Membership membership = memberships.get(subscription.membershipUuid());
		return membership != null && membership.creatorUuid().equals(creatorUuid);
	}

	private Optional<JoinedMembershipResult> toJoinedResult(MembershipSubscription subscription) {
		Membership membership = memberships.get(subscription.membershipUuid());
		if (membership == null) {
			return Optional.empty();
		}
		return Optional.of(new JoinedMembershipResult(
				subscription.subscriptionUuid(),
				subscription.membershipUuid(),
				membership.creatorUuid(),
				membership.membershipName(),
				membership.monthlyPrice(),
				MembershipApplicationPolicy.PRICE_UNIT_TOKEN,
				subscription.status(),
				subscription.startedAt(),
				subscription.endedAt()
		));
	}
}
