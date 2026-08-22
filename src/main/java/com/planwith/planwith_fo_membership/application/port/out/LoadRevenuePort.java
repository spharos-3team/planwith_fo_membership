package com.planwith.planwith_fo_membership.application.port.out;

import java.util.Optional;

import com.planwith.planwith_fo_membership.domain.model.MembershipRevenue;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.RevenueUuid;

public interface LoadRevenuePort {

	Optional<MembershipRevenue> findByUuid(RevenueUuid revenueUuid);

	Optional<MembershipRevenue> findByCreator(CreatorUuid creatorUuid);
}
