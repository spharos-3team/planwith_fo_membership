package com.planwith.planwith_fo_membership.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_membership.domain.exception.InvalidSettlementStateException;
import com.planwith.planwith_fo_membership.domain.model.vo.AdminUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.RevenueUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SettlementUuid;

class MembershipSettlementTest {

	private final SettlementUuid settlementUuid = new SettlementUuid(UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"));
	private final CreatorUuid creatorUuid = new CreatorUuid(UUID.fromString("22222222-2222-2222-2222-222222222222"));
	private final RevenueUuid revenueUuid = new RevenueUuid(UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"));
	private final AdminUuid adminUuid = AdminUuid.from("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

	@Test
	void requestApproveThenPay() {
		MembershipSettlement requested = MembershipSettlement.request(
				settlementUuid,
				creatorUuid,
				revenueUuid,
				5000L,
				Instant.parse("2026-08-22T00:00:00Z")
		);
		MembershipSettlement approved = requested.approve(adminUuid, Instant.parse("2026-08-22T00:01:00Z"));
		MembershipSettlement paid = approved.pay(adminUuid, Instant.parse("2026-08-22T00:02:00Z"));

		assertThat(requested.settlementStatus()).isEqualTo(SettlementStatus.REQUESTED);
		assertThat(approved.settlementStatus()).isEqualTo(SettlementStatus.APPROVED);
		assertThat(paid.settlementStatus()).isEqualTo(SettlementStatus.PAID);
	}

	@Test
	void requestThenReject() {
		MembershipSettlement rejected = MembershipSettlement.request(
						settlementUuid,
						creatorUuid,
						revenueUuid,
						5000L,
						Instant.parse("2026-08-22T00:00:00Z")
				)
				.reject(adminUuid, "계좌 정보 오류");

		assertThat(rejected.settlementStatus()).isEqualTo(SettlementStatus.REJECTED);
		assertThat(rejected.rejectReason()).isEqualTo("계좌 정보 오류");
	}

	@Test
	void payRejectsRequestedSettlement() {
		MembershipSettlement requested = MembershipSettlement.request(
				settlementUuid,
				creatorUuid,
				revenueUuid,
				5000L,
				Instant.parse("2026-08-22T00:00:00Z")
		);

		assertThatThrownBy(() -> requested.pay(adminUuid, Instant.parse("2026-08-22T00:02:00Z")))
				.isInstanceOf(InvalidSettlementStateException.class);
	}
}
