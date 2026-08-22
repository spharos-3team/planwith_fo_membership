package com.planwith.planwith_fo_membership.application.port.out;

import java.util.Optional;

import com.planwith.planwith_fo_membership.domain.model.MembershipSaga;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;

public interface LoadMembershipSagaPort {

	Optional<MembershipSaga> findInProgressByMemberAndCreator(MemberUuid memberUuid, CreatorUuid creatorUuid);
}
