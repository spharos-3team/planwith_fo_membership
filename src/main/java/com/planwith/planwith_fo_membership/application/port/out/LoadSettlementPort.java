package com.planwith.planwith_fo_membership.application.port.out;

import java.util.Optional;

import com.planwith.planwith_fo_membership.domain.model.MembershipSettlement;
import com.planwith.planwith_fo_membership.domain.model.vo.SettlementUuid;

public interface LoadSettlementPort {

	Optional<MembershipSettlement> findByUuid(SettlementUuid settlementUuid);
}
