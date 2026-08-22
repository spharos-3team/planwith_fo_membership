package com.planwith.planwith_fo_membership.adapter.out.persistence.revenue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.planwith.planwith_fo_membership.application.port.out.LoadRevenuePort;
import com.planwith.planwith_fo_membership.application.port.out.SaveRevenuePort;
import com.planwith.planwith_fo_membership.domain.model.MembershipRevenue;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.RevenueUuid;

public class InMemoryRevenuePort implements LoadRevenuePort, SaveRevenuePort {

	private final Map<RevenueUuid, MembershipRevenue> revenues = new HashMap<>();

	@Override
	public void save(MembershipRevenue revenue) {
		revenues.put(revenue.revenueUuid(), revenue);
	}

	@Override
	public Optional<MembershipRevenue> findByUuid(RevenueUuid revenueUuid) {
		return Optional.ofNullable(revenues.get(revenueUuid));
	}

	@Override
	public Optional<MembershipRevenue> findByCreator(CreatorUuid creatorUuid) {
		return revenues.values().stream()
				.filter(revenue -> revenue.creatorUuid().equals(creatorUuid))
				.findFirst();
	}
}
