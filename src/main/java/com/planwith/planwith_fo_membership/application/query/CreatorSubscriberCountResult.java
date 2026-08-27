package com.planwith.planwith_fo_membership.application.query;

import java.util.UUID;

public record CreatorSubscriberCountResult(
		UUID creatorUuid,
		long subscriberCount
) {
}
