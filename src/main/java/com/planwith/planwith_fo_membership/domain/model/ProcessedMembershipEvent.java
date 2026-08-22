package com.planwith.planwith_fo_membership.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;

public final class ProcessedMembershipEvent {

	private final UUID eventUuid;
	private final MemberUuid memberUuid;
	private final String eventType;
	private final Instant processedAt;

	private ProcessedMembershipEvent(UUID eventUuid, MemberUuid memberUuid, String eventType, Instant processedAt) {
		this.eventUuid = Objects.requireNonNull(eventUuid, "Event UUID is required.");
		this.memberUuid = Objects.requireNonNull(memberUuid, "Member UUID is required.");
		this.eventType = Objects.requireNonNull(eventType, "Event type is required.");
		this.processedAt = Objects.requireNonNull(processedAt, "Processed at is required.");
	}

	public static ProcessedMembershipEvent recorded(
			UUID eventUuid,
			MemberUuid memberUuid,
			String eventType,
			Instant processedAt
	) {
		return new ProcessedMembershipEvent(eventUuid, memberUuid, eventType, processedAt);
	}

	public UUID eventUuid() {
		return eventUuid;
	}

	public MemberUuid memberUuid() {
		return memberUuid;
	}

	public String eventType() {
		return eventType;
	}

	public Instant processedAt() {
		return processedAt;
	}
}
