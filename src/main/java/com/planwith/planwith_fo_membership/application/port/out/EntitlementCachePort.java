package com.planwith.planwith_fo_membership.application.port.out;

import java.util.Optional;

import com.planwith.planwith_fo_membership.domain.model.Entitlement;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;

public interface EntitlementCachePort {

	Optional<Entitlement> find(MemberUuid memberUuid, CreatorUuid creatorUuid);

	void save(Entitlement entitlement);

	void evict(MemberUuid memberUuid, CreatorUuid creatorUuid);
}
