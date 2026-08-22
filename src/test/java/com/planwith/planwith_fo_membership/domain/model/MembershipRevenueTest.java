package com.planwith.planwith_fo_membership.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_membership.domain.exception.InvalidRevenueException;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.RevenueUuid;

class MembershipRevenueTest {

	@Test
	void recordThenSettleKeepsCreatorScreenBalance() {
		MembershipRevenue revenue = MembershipRevenue.empty(
						new RevenueUuid(UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc")),
						new CreatorUuid(UUID.fromString("22222222-2222-2222-2222-222222222222"))
				)
				.record(120_000L)
				.settle(50_000L);

		assertThat(revenue.totalRevenue()).isEqualTo(120_000L);
		assertThat(revenue.availableRevenue()).isEqualTo(70_000L);
		assertThat(revenue.settledRevenue()).isEqualTo(50_000L);
	}

	@Test
	void recordThenSettleKeepsBalance() {
		MembershipRevenue revenue = MembershipRevenue.empty(
						new RevenueUuid(UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc")),
						new CreatorUuid(UUID.fromString("22222222-2222-2222-2222-222222222222"))
				)
				.record(10000L)
				.settle(3000L);

		assertThat(revenue.totalRevenue()).isEqualTo(10000L);
		assertThat(revenue.availableRevenue()).isEqualTo(7000L);
		assertThat(revenue.settledRevenue()).isEqualTo(3000L);
	}

	@Test
	void settleRejectsAmountGreaterThanAvailable() {
		MembershipRevenue revenue = MembershipRevenue.empty(
						new RevenueUuid(UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc")),
						new CreatorUuid(UUID.fromString("22222222-2222-2222-2222-222222222222"))
				)
				.record(1000L);

		assertThatThrownBy(() -> revenue.settle(1001L))
				.isInstanceOf(InvalidRevenueException.class);
	}

	@Test
	void reserveMovesAvailableToReservedWithoutSettling() {
		MembershipRevenue revenue = MembershipRevenue.empty(
						new RevenueUuid(UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc")),
						new CreatorUuid(UUID.fromString("22222222-2222-2222-2222-222222222222"))
				)
				.record(50_000L)
				.reserve(30_000L);

		assertThat(revenue.totalRevenue()).isEqualTo(50_000L);
		assertThat(revenue.availableRevenue()).isEqualTo(20_000L);
		assertThat(revenue.reservedRevenue()).isEqualTo(30_000L);
		assertThat(revenue.settledRevenue()).isZero();
	}

	@Test
	void reserveRejectsAmountGreaterThanAvailable() {
		MembershipRevenue revenue = MembershipRevenue.empty(
						new RevenueUuid(UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc")),
						new CreatorUuid(UUID.fromString("22222222-2222-2222-2222-222222222222"))
				)
				.record(50_000L)
				.reserve(30_000L);

		assertThatThrownBy(() -> revenue.reserve(30_000L))
				.isInstanceOf(InvalidRevenueException.class)
				.hasMessage("정산 가능 금액을 초과할 수 없습니다.");
	}
}
