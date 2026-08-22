package com.planwith.planwith_fo_membership.adapter.out.persistence.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.planwith.planwith_fo_membership.adapter.out.follow.InMemoryFollowQueryAdapter;
import com.planwith.planwith_fo_membership.application.command.RequestSettlementCommand;
import com.planwith.planwith_fo_membership.application.command.StartTokenPaymentCommand;
import com.planwith.planwith_fo_membership.application.port.in.command.RequestSettlementUseCase;
import com.planwith.planwith_fo_membership.application.port.in.command.StartTokenPaymentUseCase;
import com.planwith.planwith_fo_membership.application.port.out.LoadPaymentPort;
import com.planwith.planwith_fo_membership.application.port.out.LoadRevenuePort;
import com.planwith.planwith_fo_membership.application.port.out.SaveMembershipPort;
import com.planwith.planwith_fo_membership.application.port.out.SaveRevenuePort;
import com.planwith.planwith_fo_membership.application.query.StartTokenPaymentResult;
import com.planwith.planwith_fo_membership.domain.event.MembershipEventTypes;
import com.planwith.planwith_fo_membership.domain.model.Membership;
import com.planwith.planwith_fo_membership.domain.model.MembershipRevenue;
import com.planwith.planwith_fo_membership.domain.model.vo.AdminUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.RevenueUuid;

@SpringBootTest
@ActiveProfiles("test")
@Import(MembershipTransactionalOutboxFlowIntegrationTest.FollowQueryTestConfig.class)
class MembershipTransactionalOutboxFlowIntegrationTest {

	private final MemberUuid persistMemberUuid = MemberUuid.from("a1111111-1111-1111-1111-111111111111");
	private final MemberUuid rollbackMemberUuid = MemberUuid.from("a1111111-1111-1111-1111-111111111112");
	private final CreatorUuid persistCreatorUuid = CreatorUuid.from("a2222222-2222-2222-2222-222222222221");
	private final CreatorUuid rollbackCreatorUuid = CreatorUuid.from("a2222222-2222-2222-2222-222222222222");
	private final RevenueUuid persistRevenueUuid = RevenueUuid.from("a6666666-6666-6666-6666-666666666661");
	private final RevenueUuid rollbackRevenueUuid = RevenueUuid.from("a6666666-6666-6666-6666-666666666662");

	@Autowired
	private RequestSettlementUseCase requestSettlementUseCase;

	@Autowired
	private StartTokenPaymentUseCase startTokenPaymentUseCase;

	@Autowired
	private SaveRevenuePort saveRevenuePort;

	@Autowired
	private LoadRevenuePort loadRevenuePort;

	@Autowired
	private SaveMembershipPort saveMembershipPort;

	@Autowired
	private LoadPaymentPort loadPaymentPort;

	@Autowired
	private SpringDataMembershipOutboxRepository outboxRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private InMemoryFollowQueryAdapter followQueryPort;

	@BeforeEach
	void setUp() {
		followQueryPort.clear();
	}

	@Test
	void settlementRequestPersistsDomainChangeAndOutboxTogether() {
		TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
		transactionTemplate.executeWithoutResult(status -> saveRevenuePort.save(
				MembershipRevenue.empty(persistRevenueUuid, persistCreatorUuid).record(50_000L)
		));

		transactionTemplate.executeWithoutResult(status -> requestSettlementUseCase.request(
				new RequestSettlementCommand(persistCreatorUuid, 30_000L, Instant.parse("2026-08-22T01:00:00Z"))
		));

		assertThat(loadRevenuePort.findByCreator(persistCreatorUuid).orElseThrow().availableRevenue()).isEqualTo(20_000L);
		assertThat(loadRevenuePort.findByCreator(persistCreatorUuid).orElseThrow().reservedRevenue()).isEqualTo(30_000L);
		assertThat(outboxRepository.findAll())
				.anyMatch(outbox -> MembershipEventTypes.SETTLEMENT_REQUESTED.equals(outbox.eventType())
						&& outbox.publishedAt() == null);
	}

	@Test
	void rollsBackSettlementAndOutboxTogetherWhenTransactionFails() {
		TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
		transactionTemplate.executeWithoutResult(status -> saveRevenuePort.save(
				MembershipRevenue.empty(rollbackRevenueUuid, rollbackCreatorUuid).record(50_000L)
		));

		assertThatThrownBy(() -> transactionTemplate.executeWithoutResult(status -> {
			requestSettlementUseCase.request(
					new RequestSettlementCommand(rollbackCreatorUuid, 30_000L, Instant.parse("2026-08-22T01:00:00Z"))
			);
			throw new IllegalStateException("forced rollback");
		})).hasMessage("forced rollback");

		assertThat(loadRevenuePort.findByCreator(rollbackCreatorUuid).orElseThrow().availableRevenue()).isEqualTo(50_000L);
		assertThat(loadRevenuePort.findByCreator(rollbackCreatorUuid).orElseThrow().reservedRevenue()).isZero();
		assertThat(countByUuid("membership_settlement", "creator_uuid", rollbackCreatorUuid.value())).isZero();
		assertThat(countEventType(MembershipEventTypes.SETTLEMENT_REQUESTED, rollbackCreatorUuid)).isZero();
	}

	@Test
	void tokenPaymentStartPersistsPaymentAndOutboxTogether() {
		prepareApprovedMembershipAndFollow(
				persistMemberUuid,
				persistCreatorUuid,
				MembershipUuid.from("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa01")
		);
		TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

		StartTokenPaymentResult result = transactionTemplate.execute(status -> startTokenPaymentUseCase.start(
				new StartTokenPaymentCommand(persistMemberUuid, persistCreatorUuid)
		));

		assertThat(loadPaymentPort.findByUuid(result.paymentUuid())).isPresent();
		MembershipOutboxJpaEntity outbox = outboxRepository.findByEventUuid(result.paymentUuid().value()).orElseThrow();
		assertThat(outbox.eventType()).isEqualTo(MembershipEventTypes.TOKEN_DEDUCTION_REQUESTED);
		assertThat(outbox.publishedAt()).isNull();
	}

	@Test
	void rollsBackPaymentAndOutboxTogetherWhenTransactionFails() {
		prepareApprovedMembershipAndFollow(
				rollbackMemberUuid,
				rollbackCreatorUuid,
				MembershipUuid.from("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa02")
		);
		TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

		assertThatThrownBy(() -> transactionTemplate.executeWithoutResult(status -> {
			startTokenPaymentUseCase.start(new StartTokenPaymentCommand(rollbackMemberUuid, rollbackCreatorUuid));
			throw new IllegalStateException("forced rollback");
		})).hasMessage("forced rollback");

		assertThat(countByUuid("membership_saga", "member_uuid", rollbackMemberUuid.value())).isZero();
		assertThat(countEventType(MembershipEventTypes.TOKEN_DEDUCTION_REQUESTED, rollbackCreatorUuid)).isZero();
	}

	private void prepareApprovedMembershipAndFollow(
			MemberUuid memberUuid,
			CreatorUuid creatorUuid,
			MembershipUuid membershipUuid
	) {
		TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
		transactionTemplate.executeWithoutResult(status -> saveMembershipPort.save(Membership.apply(
				membershipUuid,
				creatorUuid,
				"윤휘명의 여행 멤버십",
				"월간 멤버십",
				100,
				Instant.parse("2026-08-22T00:00:00Z")
		).approve(AdminUuid.from("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"))));
		followQueryPort.follow(memberUuid, creatorUuid);
	}

	private Long countByUuid(String table, String column, Object value) {
		return jdbcTemplate.queryForObject(
				"select count(*) from " + table + " where " + column + " = ?",
				Long.class,
				value.toString()
		);
	}

	private Long countEventType(String eventType, CreatorUuid creatorUuid) {
		return jdbcTemplate.queryForObject(
				"select count(*) from membership_outbox where event_type = ? and payload like ?",
				Long.class,
				eventType,
				"%" + creatorUuid.value() + "%"
		);
	}

	@TestConfiguration
	static class FollowQueryTestConfig {

		@Bean
		@Primary
		InMemoryFollowQueryAdapter inMemoryFollowQueryAdapter() {
			return new InMemoryFollowQueryAdapter();
		}
	}
}
