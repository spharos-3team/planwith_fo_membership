package com.planwith.planwith_fo_membership.domain.model;

import java.time.Instant;
import java.util.Objects;

import com.planwith.planwith_fo_membership.domain.exception.InvalidMembershipStateException;
import com.planwith.planwith_fo_membership.domain.model.vo.AdminUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;

public final class Membership {

	private static final int NAME_MAX_LENGTH = 100;
	private static final int DESCRIPTION_MAX_LENGTH = 1000;
	private static final int REJECT_REASON_MAX_LENGTH = 100;

	private final MembershipUuid membershipUuid;
	private final AdminUuid adminUuid;
	private final CreatorUuid creatorUuid;
	private final String membershipName;
	private final String description;
	private final int monthlyPrice;
	private final MembershipStatus status;
	private final String rejectReason;
	private final Instant createAt;

	private Membership(
			MembershipUuid membershipUuid,
			AdminUuid adminUuid,
			CreatorUuid creatorUuid,
			String membershipName,
			String description,
			int monthlyPrice,
			MembershipStatus status,
			String rejectReason,
			Instant createAt
	) {
		this.membershipUuid = Objects.requireNonNull(membershipUuid, "Membership UUID is required.");
		this.adminUuid = adminUuid;
		this.creatorUuid = Objects.requireNonNull(creatorUuid, "Creator UUID is required.");
		this.membershipName = requireName(membershipName);
		this.description = requireDescription(description);
		this.monthlyPrice = requireMonthlyPrice(monthlyPrice);
		this.status = Objects.requireNonNull(status, "Membership status is required.");
		this.rejectReason = rejectReason;
		this.createAt = Objects.requireNonNull(createAt, "Create at is required.");
	}

	public static Membership apply(
			MembershipUuid membershipUuid,
			CreatorUuid creatorUuid,
			String membershipName,
			String description,
			int monthlyPrice,
			Instant createAt
	) {
		return new Membership(
				membershipUuid,
				null,
				creatorUuid,
				membershipName,
				description,
				monthlyPrice,
				MembershipStatus.PENDING,
				null,
				createAt
		);
	}

	public static Membership restore(
			MembershipUuid membershipUuid,
			AdminUuid adminUuid,
			CreatorUuid creatorUuid,
			String membershipName,
			String description,
			int monthlyPrice,
			MembershipStatus status,
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

	public Membership approve(AdminUuid adminUuid) {
		if (status != MembershipStatus.PENDING) {
			throw new InvalidMembershipStateException("대기 중인 멤버십만 승인할 수 있습니다.");
		}
		return new Membership(
				membershipUuid,
				Objects.requireNonNull(adminUuid, "Admin UUID is required."),
				creatorUuid,
				membershipName,
				description,
				monthlyPrice,
				MembershipStatus.APPROVED,
				null,
				createAt
		);
	}

	public Membership reject(AdminUuid adminUuid, String reason) {
		if (status != MembershipStatus.PENDING) {
			throw new InvalidMembershipStateException("대기 중인 멤버십만 거절할 수 있습니다.");
		}
		return new Membership(
				membershipUuid,
				Objects.requireNonNull(adminUuid, "Admin UUID is required."),
				creatorUuid,
				membershipName,
				description,
				monthlyPrice,
				MembershipStatus.REJECTED,
				requireRejectReason(reason),
				createAt
		);
	}

	public Membership deactivate() {
		if (status != MembershipStatus.APPROVED) {
			throw new InvalidMembershipStateException("승인된 멤버십만 운영 종료할 수 있습니다.");
		}
		return new Membership(
				membershipUuid,
				adminUuid,
				creatorUuid,
				membershipName,
				description,
				monthlyPrice,
				MembershipStatus.INACTIVE,
				rejectReason,
				createAt
		);
	}

	public Membership changeMonthlyPrice(int nextMonthlyPrice) {
		if (status != MembershipStatus.APPROVED) {
			throw new InvalidMembershipStateException("승인된 멤버십만 월 구독 토큰을 변경할 수 있습니다.");
		}
		return new Membership(
				membershipUuid,
				adminUuid,
				creatorUuid,
				membershipName,
				description,
				requireMonthlyPrice(nextMonthlyPrice),
				status,
				rejectReason,
				createAt
		);
	}

	public boolean isApproved() {
		return status == MembershipStatus.APPROVED;
	}

	public MembershipUuid membershipUuid() {
		return membershipUuid;
	}

	public AdminUuid adminUuid() {
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

	public MembershipStatus status() {
		return status;
	}

	public String rejectReason() {
		return rejectReason;
	}

	public Instant createAt() {
		return createAt;
	}

	private static String requireName(String membershipName) {
		if (membershipName == null || membershipName.isBlank()) {
			throw new InvalidMembershipStateException("멤버십 이름은 필수입니다.");
		}
		if (membershipName.length() > NAME_MAX_LENGTH) {
			throw new InvalidMembershipStateException("멤버십 이름은 100자를 초과할 수 없습니다.");
		}
		return membershipName;
	}

	private static String requireDescription(String description) {
		if (description != null && description.length() > DESCRIPTION_MAX_LENGTH) {
			throw new InvalidMembershipStateException("멤버십 설명은 1000자를 초과할 수 없습니다.");
		}
		return description;
	}

	private static int requireMonthlyPrice(int monthlyPrice) {
		if (monthlyPrice <= 0) {
			throw new InvalidMembershipStateException("월 구독 금액은 0보다 커야 합니다.");
		}
		return monthlyPrice;
	}

	private static String requireRejectReason(String reason) {
		if (reason == null || reason.isBlank()) {
			throw new InvalidMembershipStateException("거절 사유는 필수입니다.");
		}
		if (reason.length() > REJECT_REASON_MAX_LENGTH) {
			throw new InvalidMembershipStateException("거절 사유는 100자를 초과할 수 없습니다.");
		}
		return reason;
	}
}
