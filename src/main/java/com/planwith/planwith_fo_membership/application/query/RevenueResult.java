package com.planwith.planwith_fo_membership.application.query;

import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.RevenueUuid;

public record RevenueResult(
		RevenueUuid revenueUuid,
		CreatorUuid creatorUuid,
		long totalRevenue,
		long availableRevenue,
		long settledRevenue
) {

	public static RevenueResult empty(CreatorUuid creatorUuid) {
		return new RevenueResult(null, creatorUuid, 0L, 0L, 0L);
	}
}
