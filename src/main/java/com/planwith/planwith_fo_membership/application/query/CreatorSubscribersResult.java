package com.planwith.planwith_fo_membership.application.query;

import java.util.List;

public record CreatorSubscribersResult(
		int subscriberCount,
		List<CreatorSubscriberResult> subscribers
) {
}
