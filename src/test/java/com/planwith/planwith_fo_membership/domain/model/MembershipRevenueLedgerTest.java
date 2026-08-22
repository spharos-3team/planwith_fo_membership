package com.planwith.planwith_fo_membership.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_membership.domain.exception.InvalidRevenueException;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.LedgerUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.PaymentUuid;
import com.planwith.planwith_fo_membership.domain.service.RevenueSharePolicy;

class MembershipRevenueLedgerTest {

	@Test
	void recordedLedgerKeepsShareInvariant() {
		MembershipRevenueLedger ledger = MembershipRevenueLedger.recorded(
				new LedgerUuid(UUID.fromString("77777777-7777-7777-7777-777777777777")),
				new PaymentUuid(UUID.fromString("44444444-4444-4444-4444-444444444444")),
				new CreatorUuid(UUID.fromString("22222222-2222-2222-2222-222222222222")),
				RevenueSharePolicy.split(100L),
				Instant.parse("2026-08-22T00:10:00Z")
		);

		assertThat(ledger.tokenAmount()).isEqualTo(100L);
		assertThat(ledger.grossKrw()).isEqualTo(10_000L);
		assertThat(ledger.companyShareKrw()).isEqualTo(7_000L);
		assertThat(ledger.creatorShareKrw()).isEqualTo(3_000L);
		assertThat(ledger.companyShareKrw() + ledger.creatorShareKrw()).isEqualTo(ledger.grossKrw());
	}

	@Test
	void restoreRejectsShareSumMismatch() {
		assertThatThrownBy(() -> MembershipRevenueLedger.restore(
				new LedgerUuid(UUID.fromString("77777777-7777-7777-7777-777777777777")),
				new PaymentUuid(UUID.fromString("44444444-4444-4444-4444-444444444444")),
				new CreatorUuid(UUID.fromString("22222222-2222-2222-2222-222222222222")),
				100L,
				10_000L,
				7_000L,
				2_999L,
				Instant.parse("2026-08-22T00:10:00Z")
		)).isInstanceOf(InvalidRevenueException.class);
	}
}
