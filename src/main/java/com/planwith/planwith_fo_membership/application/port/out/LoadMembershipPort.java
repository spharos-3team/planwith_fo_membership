package com.planwith.planwith_fo_membership.application.port.out;

import java.util.Optional;

import com.planwith.planwith_fo_membership.domain.model.Membership;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;

public interface LoadMembershipPort {

	Optional<Membership> findByUuid(MembershipUuid membershipUuid);

	Optional<Membership> findLatestByCreator(CreatorUuid creatorUuid);
}
