package com.planwith.planwith_fo_membership.adapter.out.persistence.settlement;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_membership.application.port.out.LoadSettlementPort;
import com.planwith.planwith_fo_membership.application.port.out.SaveSettlementPort;
import com.planwith.planwith_fo_membership.domain.model.MembershipSettlement;
import com.planwith.planwith_fo_membership.domain.model.SettlementStatus;
import com.planwith.planwith_fo_membership.domain.model.vo.AdminUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.RevenueUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SettlementUuid;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MembershipSettlementPersistenceAdapterIntegrationTest {

	@Autowired
	private SaveSettlementPort saveSettlementPort;

	@Autowired
	private LoadSettlementPort loadSettlementPort;

	@Test
	void saveAndLoadSettlementStatusTransition() {
		SettlementUuid settlementUuid = SettlementUuid.from("77777777-7777-7777-7777-777777777777");
		CreatorUuid creatorUuid = CreatorUuid.from("22222222-2222-2222-2222-222222222222");
		RevenueUuid revenueUuid = RevenueUuid.from("66666666-6666-6666-6666-666666666666");
		Instant requestedAt = Instant.parse("2026-08-22T01:00:00Z");
		Instant approvedAt = Instant.parse("2026-08-22T01:05:00Z");

		saveSettlementPort.save(MembershipSettlement.request(
				settlementUuid,
				creatorUuid,
				revenueUuid,
				10000L,
				requestedAt
		));
		MembershipSettlement requested = loadSettlementPort.findByUuid(settlementUuid).orElseThrow();
		assertThat(requested.settlementStatus()).isEqualTo(SettlementStatus.REQUESTED);
		assertThat(requested.approvedAt()).isNull();

		saveSettlementPort.save(requested.approve(AdminUuid.from("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"), approvedAt));
		MembershipSettlement approved = loadSettlementPort.findByUuid(settlementUuid).orElseThrow();
		assertThat(approved.settlementAmount()).isEqualTo(10000L);
		assertThat(approved.settlementStatus()).isEqualTo(SettlementStatus.APPROVED);
		assertThat(approved.approvedAt()).isEqualTo(approvedAt);
		assertThat(approved.processedBy()).isEqualTo(AdminUuid.from("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"));
		assertThat(approved.creatorUuid()).isEqualTo(creatorUuid);
		assertThat(approved.revenueUuid()).isEqualTo(revenueUuid);
	}
}
