package com.planwith.planwith_fo_membership.adapter.out.persistence.settlement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.planwith.planwith_fo_membership.application.port.out.LoadSettlementPort;
import com.planwith.planwith_fo_membership.application.port.out.SaveSettlementPort;
import com.planwith.planwith_fo_membership.domain.model.MembershipSettlement;
import com.planwith.planwith_fo_membership.domain.model.vo.SettlementUuid;

public class InMemorySettlementPort implements LoadSettlementPort, SaveSettlementPort {

	private final Map<SettlementUuid, MembershipSettlement> settlements = new HashMap<>();

	@Override
	public void save(MembershipSettlement settlement) {
		settlements.put(settlement.settlementUuid(), settlement);
	}

	@Override
	public Optional<MembershipSettlement> findByUuid(SettlementUuid settlementUuid) {
		return Optional.ofNullable(settlements.get(settlementUuid));
	}

	public List<MembershipSettlement> settlements() {
		return List.copyOf(settlements.values());
	}
}
