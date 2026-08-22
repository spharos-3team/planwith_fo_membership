package com.planwith.planwith_fo_membership.adapter.out.persistence.revenue;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_membership.application.port.out.LoadRevenueLedgerPort;
import com.planwith.planwith_fo_membership.application.port.out.SaveRevenueLedgerPort;
import com.planwith.planwith_fo_membership.domain.model.MembershipRevenueLedger;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.LedgerUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.PaymentUuid;
import com.planwith.planwith_fo_membership.domain.service.RevenueSharePolicy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MembershipRevenueLedgerPersistenceAdapterIntegrationTest {

	@Autowired
	private SaveRevenueLedgerPort saveRevenueLedgerPort;

	@Autowired
	private LoadRevenueLedgerPort loadRevenueLedgerPort;

	@Test
	void saveAndLoadLedgerByPaymentUuidWithoutOverwrite() {
		PaymentUuid paymentUuid = PaymentUuid.from("44444444-4444-4444-4444-444444444444");
		CreatorUuid creatorUuid = CreatorUuid.from("22222222-2222-2222-2222-222222222222");
		MembershipRevenueLedger recorded = MembershipRevenueLedger.recorded(
				LedgerUuid.from("77777777-7777-7777-7777-777777777777"),
				paymentUuid,
				creatorUuid,
				RevenueSharePolicy.split(100L),
				Instant.parse("2026-08-22T00:10:00Z")
		);
		saveRevenueLedgerPort.save(recorded);
		saveRevenueLedgerPort.save(MembershipRevenueLedger.recorded(
				LedgerUuid.from("88888888-8888-8888-8888-888888888888"),
				paymentUuid,
				creatorUuid,
				RevenueSharePolicy.split(100L),
				Instant.parse("2026-08-22T00:11:00Z")
		));

		MembershipRevenueLedger loaded = loadRevenueLedgerPort.findByPaymentUuid(paymentUuid).orElseThrow();
		assertThat(loaded.ledgerUuid()).isEqualTo(recorded.ledgerUuid());
		assertThat(loaded.grossKrw()).isEqualTo(10_000L);
		assertThat(loaded.companyShareKrw()).isEqualTo(7_000L);
		assertThat(loaded.creatorShareKrw()).isEqualTo(3_000L);
	}
}
