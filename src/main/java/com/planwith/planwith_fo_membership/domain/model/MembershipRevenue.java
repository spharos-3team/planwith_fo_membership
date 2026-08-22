package com.planwith.planwith_fo_membership.domain.model;

import java.util.Objects;

import com.planwith.planwith_fo_membership.domain.exception.InvalidRevenueException;
import com.planwith.planwith_fo_membership.domain.exception.SettlementAmountExceededException;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.RevenueUuid;

public final class MembershipRevenue {

	private final RevenueUuid revenueUuid;
	private final CreatorUuid creatorUuid;
	private final long totalRevenue;
	private final long availableRevenue;
	private final long reservedRevenue;
	private final long settledRevenue;

	private MembershipRevenue(
			RevenueUuid revenueUuid,
			CreatorUuid creatorUuid,
			long totalRevenue,
			long availableRevenue,
			long reservedRevenue,
			long settledRevenue
	) {
		this.revenueUuid = Objects.requireNonNull(revenueUuid, "Revenue UUID is required.");
		this.creatorUuid = Objects.requireNonNull(creatorUuid, "Creator UUID is required.");
		if (totalRevenue < 0 || availableRevenue < 0 || reservedRevenue < 0 || settledRevenue < 0) {
			throw new InvalidRevenueException("수익 금액은 음수일 수 없습니다.");
		}
		if (availableRevenue + reservedRevenue + settledRevenue != totalRevenue) {
			throw new InvalidRevenueException("정산 가능 금액, 정산 신청 중 금액, 정산 완료 금액의 합은 총 수익과 같아야 합니다.");
		}
		this.totalRevenue = totalRevenue;
		this.availableRevenue = availableRevenue;
		this.reservedRevenue = reservedRevenue;
		this.settledRevenue = settledRevenue;
	}

	public static MembershipRevenue empty(RevenueUuid revenueUuid, CreatorUuid creatorUuid) {
		return new MembershipRevenue(revenueUuid, creatorUuid, 0L, 0L, 0L, 0L);
	}

	public static MembershipRevenue restore(
			RevenueUuid revenueUuid,
			CreatorUuid creatorUuid,
			long totalRevenue,
			long availableRevenue,
			long reservedRevenue,
			long settledRevenue
	) {
		return new MembershipRevenue(
				revenueUuid,
				creatorUuid,
				totalRevenue,
				availableRevenue,
				reservedRevenue,
				settledRevenue
		);
	}

	public MembershipRevenue record(long amount) {
		if (amount <= 0) {
			throw new InvalidRevenueException("적립 금액은 0보다 커야 합니다.");
		}
		return new MembershipRevenue(
				revenueUuid,
				creatorUuid,
				totalRevenue + amount,
				availableRevenue + amount,
				reservedRevenue,
				settledRevenue
		);
	}

	public MembershipRevenue reserve(long amount) {
		if (amount <= 0) {
			throw new InvalidRevenueException("정산 신청 금액은 0보다 커야 합니다.");
		}
		if (amount > availableRevenue) {
			throw new SettlementAmountExceededException("정산 가능 금액을 초과할 수 없습니다.");
		}
		return new MembershipRevenue(
				revenueUuid,
				creatorUuid,
				totalRevenue,
				availableRevenue - amount,
				reservedRevenue + amount,
				settledRevenue
		);
	}

	public MembershipRevenue confirmReserved(long amount) {
		if (amount <= 0) {
			throw new InvalidRevenueException("정산 금액은 0보다 커야 합니다.");
		}
		if (amount > reservedRevenue) {
			throw new InvalidRevenueException("정산 신청 중 금액을 초과할 수 없습니다.");
		}
		return new MembershipRevenue(
				revenueUuid,
				creatorUuid,
				totalRevenue,
				availableRevenue,
				reservedRevenue - amount,
				settledRevenue + amount
		);
	}

	public MembershipRevenue releaseReserved(long amount) {
		if (amount <= 0) {
			throw new InvalidRevenueException("정산 금액은 0보다 커야 합니다.");
		}
		if (amount > reservedRevenue) {
			throw new InvalidRevenueException("정산 신청 중 금액을 초과할 수 없습니다.");
		}
		return new MembershipRevenue(
				revenueUuid,
				creatorUuid,
				totalRevenue,
				availableRevenue + amount,
				reservedRevenue - amount,
				settledRevenue
		);
	}

	public MembershipRevenue settle(long amount) {
		if (amount <= 0) {
			throw new InvalidRevenueException("정산 금액은 0보다 커야 합니다.");
		}
		if (amount > availableRevenue) {
			throw new SettlementAmountExceededException("정산 가능 금액을 초과할 수 없습니다.");
		}
		return new MembershipRevenue(
				revenueUuid,
				creatorUuid,
				totalRevenue,
				availableRevenue - amount,
				reservedRevenue,
				settledRevenue + amount
		);
	}

	public RevenueUuid revenueUuid() {
		return revenueUuid;
	}

	public CreatorUuid creatorUuid() {
		return creatorUuid;
	}

	public long totalRevenue() {
		return totalRevenue;
	}

	public long availableRevenue() {
		return availableRevenue;
	}

	public long reservedRevenue() {
		return reservedRevenue;
	}

	public long settledRevenue() {
		return settledRevenue;
	}
}
