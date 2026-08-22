package com.planwith.planwith_fo_membership.domain.model;

import java.time.Instant;
import java.util.Objects;

import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;

public final class Membership {

	private final MembershipUuid membershipUuid;
	private final String adminUuid;
	private final CreatorUuid creatorUuid;
	private final String membershipName;
	private final String description;
	private final int monthlyPrice;
	private final String status;
	private final String rejectReason;
	private final Instant createAt;

	private Membership(
			MembershipUuid membershipUuid,
			String adminUuid,
			CreatorUuid creatorUuid,
			String membershipName,
			String description,
			int monthlyPrice,
			String status,
			String rejectReason,
			Instant createAt
	) {
		this.membershipUuid = Objects.requireNonNull(membershipUuid, "Membership UUID is required.");
		this.adminUuid = adminUuid;
		this.creatorUuid = Objects.requireNonNull(creatorUuid, "Creator UUID is required.");
		this.membershipName = Objects.requireNonNull(membershipName, "Membership name is required.");
		this.description = description;
		this.monthlyPrice = monthlyPrice;
		this.status = Objects.requireNonNull(status, "Membership status is required.");
		this.rejectReason = rejectReason;
		this.createAt = Objects.requireNonNull(createAt, "Create at is required.");
	}

	public static Membership restore(
			MembershipUuid membershipUuid,
			String adminUuid,
			CreatorUuid creatorUuid,
			String membershipName,
			String description,
			int monthlyPrice,
			String status,
			String rejectReason,
			Instant createAt
	) {
		return new Membership(
				membershipUuid,
				adminUuid,
				creatorUuid,
				membershipName,
				description,
				monthlyPrice,
				status,
				rejectReason,
				createAt
		);
	}

	public MembershipUuid membershipUuid() {
		return membershipUuid;
	}

	public String adminUuid() {
		return adminUuid;
	}

	public CreatorUuid creatorUuid() {
		return creatorUuid;
	}

	public String membershipName() {
		return membershipName;
	}

	public String description() {
		return description;
	}

	public int monthlyPrice() {
		return monthlyPrice;
	}

	public String status() {
		return status;
	}

	public String rejectReason() {
		return rejectReason;
	}

	public Instant createAt() {
		return createAt;
	}
}
