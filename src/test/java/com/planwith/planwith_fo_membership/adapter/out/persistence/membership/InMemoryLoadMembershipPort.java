package com.planwith.planwith_fo_membership.adapter.out.persistence.membership;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.planwith.planwith_fo_membership.application.port.out.LoadMembershipPort;
import com.planwith.planwith_fo_membership.domain.model.Membership;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;
import com.planwith.planwith_fo_membership.domain.service.MembershipApplicationPolicy;

public class InMemoryLoadMembershipPort implements LoadMembershipPort {

	private final Map<MembershipUuid, Membership> memberships = new HashMap<>();

	public void save(Membership membership) {
		memberships.put(membership.membershipUuid(), membership);
	}

	@Override
	public Optional<Membership> findByUuid(MembershipUuid membershipUuid) {
		return Optional.ofNullable(memberships.get(membershipUuid));
	}

	@Override
	public Optional<Membership> findLatestByCreator(CreatorUuid creatorUuid) {
		return memberships.values().stream()
				.filter(membership -> membership.creatorUuid().equals(creatorUuid))
				.findFirst();
	}

	@Override
	public Optional<Membership> findOpenByCreator(CreatorUuid creatorUuid) {
		return memberships.values().stream()
				.filter(membership -> membership.creatorUuid().equals(creatorUuid))
				.filter(MembershipApplicationPolicy::isDuplicateApplication)
				.findFirst();
	}
}
