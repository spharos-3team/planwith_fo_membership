package com.planwith.planwith_fo_membership.adapter.out.persistence.subscription;

import java.time.Instant;
import java.util.UUID;

import com.planwith.planwith_fo_membership.domain.model.SubscriptionStatus;

interface JoinedMembershipRow {

	UUID getSubscriptionUuid();

	UUID getMembershipUuid();

	UUID getCreatorUuid();

	String getMembershipName();

	Integer getMonthlyPrice();

	SubscriptionStatus getStatus();

	Instant getStartedAt();

	Instant getEndedAt();
}
