package com.planwith.planwith_fo_membership.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_membership.domain.exception.InvalidRevenueException;
import com.planwith.planwith_fo_membership.domain.service.RevenueSharePolicy.RevenueShareResult;

class RevenueSharePolicyTest {

	@Test
	void oneHundredTokensSplitToCompanySeventyCreatorThirtyInKrw() {
		RevenueShareResult share = RevenueSharePolicy.split(100L);

		assertThat(share.tokenAmount()).isEqualTo(100L);
		assertThat(share.grossKrw()).isEqualTo(10_000L);
		assertThat(share.companyShareKrw()).isEqualTo(7_000L);
		assertThat(share.creatorShareKrw()).isEqualTo(3_000L);
		assertThat(share.companyShareKrw() + share.creatorShareKrw()).isEqualTo(share.grossKrw());
	}

	@Test
	void remainderGoesToCompanySoSharesSumToGross() {
		RevenueShareResult share = RevenueSharePolicy.split(101L);

		assertThat(share.grossKrw()).isEqualTo(10_100L);
		assertThat(share.creatorShareKrw()).isEqualTo(3_030L);
		assertThat(share.companyShareKrw()).isEqualTo(7_070L);
		assertThat(share.companyShareKrw() + share.creatorShareKrw()).isEqualTo(share.grossKrw());
	}

	@Test
	void rejectsNonPositiveTokenAmount() {
		assertThatThrownBy(() -> RevenueSharePolicy.split(0L))
				.isInstanceOf(InvalidRevenueException.class);
		assertThatThrownBy(() -> RevenueSharePolicy.split(-1L))
				.isInstanceOf(InvalidRevenueException.class);
	}
}
