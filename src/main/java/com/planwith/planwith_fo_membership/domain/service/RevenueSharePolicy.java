package com.planwith.planwith_fo_membership.domain.service;

import com.planwith.planwith_fo_membership.domain.exception.InvalidRevenueException;

public final class RevenueSharePolicy {

	public static final long TOKEN_TO_KRW = 100L;
	public static final int COMPANY_PERCENT = 70;
	public static final int CREATOR_PERCENT = 30;

	private RevenueSharePolicy() {
	}

	public static RevenueShareResult split(long tokenAmount) {
		if (tokenAmount <= 0) {
			throw new InvalidRevenueException("수익 배분 대상 토큰 금액은 0보다 커야 합니다.");
		}
		long grossKrw = tokenAmount * TOKEN_TO_KRW;
		long creatorShareKrw = grossKrw * CREATOR_PERCENT / 100;
		long companyShareKrw = grossKrw - creatorShareKrw;
		return new RevenueShareResult(tokenAmount, grossKrw, companyShareKrw, creatorShareKrw);
	}

	public record RevenueShareResult(
			long tokenAmount,
			long grossKrw,
			long companyShareKrw,
			long creatorShareKrw
	) {

		public RevenueShareResult {
			if (companyShareKrw + creatorShareKrw != grossKrw) {
				throw new InvalidRevenueException("회사 수익과 Creator 수익의 합은 총 환산 금액과 같아야 합니다.");
			}
		}
	}
}
