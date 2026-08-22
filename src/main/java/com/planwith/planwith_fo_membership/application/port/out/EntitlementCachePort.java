package com.planwith.planwith_fo_membership.application.port.out;

import java.util.Optional;

import com.planwith.planwith_fo_membership.application.query.ContentAccessResult;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;

public interface EntitlementCachePort {

	Optional<ContentAccessResult> find(MemberUuid memberUuid, CreatorUuid creatorUuid);

	void save(ContentAccessResult result);

	void evict(MemberUuid memberUuid, CreatorUuid creatorUuid);
}
