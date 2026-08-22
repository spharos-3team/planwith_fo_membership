package com.planwith.planwith_fo_membership.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_membership.adapter.in.kafka.TokenDeductionFailedEventConsumer;
import com.planwith.planwith_fo_membership.adapter.in.kafka.TokenDeductionSucceededEventConsumer;
import com.planwith.planwith_fo_membership.adapter.out.follow.InMemoryFollowQueryAdapter;
import com.planwith.planwith_fo_membership.adapter.out.grade.InMemoryGradeQueryAdapter;
import com.planwith.planwith_fo_membership.adapter.out.redis.InMemoryEntitlementCacheAdapter;
import com.planwith.planwith_fo_membership.application.port.in.command.HandleTokenDeductionFailedUseCase;
import com.planwith.planwith_fo_membership.application.port.in.command.HandleTokenDeductionSucceededUseCase;
import com.planwith.planwith_fo_membership.application.port.out.LoadMembershipPort;
import com.planwith.planwith_fo_membership.application.port.out.LoadPaymentPort;
import com.planwith.planwith_fo_membership.application.port.out.LoadRevenueLedgerPort;
import com.planwith.planwith_fo_membership.application.port.out.LoadRevenuePort;
import com.planwith.planwith_fo_membership.application.port.out.LoadSettlementPort;
import com.planwith.planwith_fo_membership.application.port.out.LoadSubscriptionPort;
import com.planwith.planwith_fo_membership.application.port.out.ProcessedMembershipEventPort;
import com.planwith.planwith_fo_membership.application.query.MemberGradeResult;
import com.planwith.planwith_fo_membership.config.MembershipKafkaProperties;
import com.planwith.planwith_fo_membership.domain.event.MembershipEventTypes;
import com.planwith.planwith_fo_membership.domain.exception.MembershipErrorCodes;
import com.planwith.planwith_fo_membership.domain.model.Membership;
import com.planwith.planwith_fo_membership.domain.model.MembershipPayment;
import com.planwith.planwith_fo_membership.domain.model.MembershipRevenue;
import com.planwith.planwith_fo_membership.domain.model.MembershipRevenueLedger;
import com.planwith.planwith_fo_membership.domain.model.MembershipSettlement;
import com.planwith.planwith_fo_membership.domain.model.MembershipStatus;
import com.planwith.planwith_fo_membership.domain.model.PaymentStatus;
import com.planwith.planwith_fo_membership.domain.model.SettlementStatus;
import com.planwith.planwith_fo_membership.domain.model.SubscriptionStatus;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.PaymentUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SettlementUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;
import com.planwith.planwith_fo_membership.domain.service.MembershipApplicationPolicy;
import com.planwith.planwith_fo_membership.domain.service.RevenueSharePolicy;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(MembershipEventStormingIntegrationTest.ExternalPortTestConfig.class)
@DisplayName("20. 통합 테스트 - 신청 → 승인 → 가입 → 토큰 차감 → 수익 → 정산")
class MembershipEventStormingIntegrationTest {

	private static final int MONTHLY_PRICE = 100;
	private static final long GROSS_KRW = MONTHLY_PRICE * RevenueSharePolicy.TOKEN_TO_KRW;
	private static final long CREATOR_SHARE_KRW = GROSS_KRW * RevenueSharePolicy.CREATOR_PERCENT / 100;
	private static final long COMPANY_SHARE_KRW = GROSS_KRW - CREATOR_SHARE_KRW;
	private static final String API = "/api/planwith-fo-membership";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private InMemoryGradeQueryAdapter gradeQueryPort;

	@Autowired
	private InMemoryFollowQueryAdapter followQueryPort;

	@Autowired
	private InMemoryEntitlementCacheAdapter entitlementCacheAdapter;

	@Autowired
	private LoadMembershipPort loadMembershipPort;

	@Autowired
	private LoadRevenuePort loadRevenuePort;

	@Autowired
	private LoadPaymentPort loadPaymentPort;

	@Autowired
	private LoadSubscriptionPort loadSubscriptionPort;

	@Autowired
	private LoadRevenueLedgerPort loadRevenueLedgerPort;

	@Autowired
	private LoadSettlementPort loadSettlementPort;

	@Autowired
	private ProcessedMembershipEventPort processedMembershipEventPort;

	@Autowired
	private HandleTokenDeductionSucceededUseCase handleTokenDeductionSucceededUseCase;

	@Autowired
	private HandleTokenDeductionFailedUseCase handleTokenDeductionFailedUseCase;

	@Autowired
	private MembershipKafkaProperties kafkaProperties;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private TokenDeductionSucceededEventConsumer tokenSucceededConsumer;
	private TokenDeductionFailedEventConsumer tokenFailedConsumer;

	@BeforeEach
	void setUp() {
		gradeQueryPort.clear();
		followQueryPort.clear();
		entitlementCacheAdapter.clear();
		tokenSucceededConsumer = new TokenDeductionSucceededEventConsumer(
				objectMapper,
				handleTokenDeductionSucceededUseCase,
				kafkaProperties
		);
		tokenFailedConsumer = new TokenDeductionFailedEventConsumer(
				objectMapper,
				handleTokenDeductionFailedUseCase,
				kafkaProperties
		);
	}

	@Test
	@DisplayName("1. Explorer 미만 → 멤버십 개설 실패")
	void scenario01_belowExplorerCannotOpenMembership() throws Exception {
		UUID creatorUuid = UUID.randomUUID();
		prepareGrade(creatorUuid, "ROOKIE", "루키", 1);

		mockMvc.perform(post(API + "/memberships/applications")
						.header("X-Member-UUID", creatorUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content(applicationBody()))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value(MembershipErrorCodes.MEMBERSHIP_GRADE_NOT_ELIGIBLE))
				.andExpect(jsonPath("$.message").value("Explorer 이상 등급만 멤버십을 개설할 수 있습니다."));

		assertThat(loadMembershipPort.findOpenByCreator(new CreatorUuid(creatorUuid))).isEmpty();
		assertThat(loadRevenuePort.findByCreator(new CreatorUuid(creatorUuid))).isEmpty();
	}

	@Test
	@DisplayName("2. Explorer 이상 → 멤버십 신청 성공")
	void scenario02_explorerCanApplyMembership() throws Exception {
		UUID creatorUuid = UUID.randomUUID();
		prepareExplorer(creatorUuid);

		MvcResult result = mockMvc.perform(post(API + "/memberships/applications")
						.header("X-Member-UUID", creatorUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content(applicationBody()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.creatorUuid").value(creatorUuid.toString()))
				.andExpect(jsonPath("$.membershipName").value("윤휘명의 여행 멤버십"))
				.andExpect(jsonPath("$.monthlyPrice").value(MONTHLY_PRICE))
				.andExpect(jsonPath("$.priceUnit").value(MembershipApplicationPolicy.PRICE_UNIT_TOKEN))
				.andExpect(jsonPath("$.status").value(MembershipStatus.PENDING.name()))
				.andReturn();

		UUID membershipUuid = uuid(result, "membershipUuid");
		Membership membership = loadMembershipPort.findByUuid(new MembershipUuid(membershipUuid)).orElseThrow();
		assertThat(membership.status()).isEqualTo(MembershipStatus.PENDING);
		assertThat(membership.monthlyPrice()).isEqualTo(MONTHLY_PRICE);
	}

	@Test
	@DisplayName("3. 신청 시 Revenue 초기화")
	void scenario03_applyInitializesEmptyRevenue() throws Exception {
		UUID creatorUuid = UUID.randomUUID();
		prepareExplorer(creatorUuid);

		MvcResult result = mockMvc.perform(post(API + "/memberships/applications")
						.header("X-Member-UUID", creatorUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content(applicationBody()))
				.andExpect(status().isCreated())
				.andReturn();

		UUID revenueUuid = uuid(result, "revenueUuid");
		MembershipRevenue revenue = loadRevenuePort.findByCreator(new CreatorUuid(creatorUuid)).orElseThrow();
		assertThat(revenue.revenueUuid().value()).isEqualTo(revenueUuid);
		assertThat(revenue.totalRevenue()).isZero();
		assertThat(revenue.availableRevenue()).isZero();
		assertThat(revenue.reservedRevenue()).isZero();
		assertThat(revenue.settledRevenue()).isZero();
	}

	@Test
	@DisplayName("4. 관리자 멤버십 승인")
	void scenario04_adminApprovesMembership() throws Exception {
		Flow flow = applyPendingMembership();

		mockMvc.perform(post(API + "/admin/memberships/" + flow.membershipUuid + "/approve")
						.header("X-Admin-UUID", flow.adminUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(MembershipStatus.APPROVED.name()))
				.andExpect(jsonPath("$.adminUuid").value(flow.adminUuid.toString()));

		Membership membership = loadMembershipPort.findByUuid(new MembershipUuid(flow.membershipUuid)).orElseThrow();
		assertThat(membership.status()).isEqualTo(MembershipStatus.APPROVED);
		assertThat(membership.adminUuid().value()).isEqualTo(flow.adminUuid);
		assertThat(membership.rejectReason()).isNull();
	}

	@Test
	@DisplayName("5. 관리자 멤버십 거절")
	void scenario05_adminRejectsMembership() throws Exception {
		Flow flow = applyPendingMembership();

		mockMvc.perform(post(API + "/admin/memberships/" + flow.membershipUuid + "/reject")
						.header("X-Admin-UUID", flow.adminUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"rejectReason":"서류 미비"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(MembershipStatus.REJECTED.name()))
				.andExpect(jsonPath("$.rejectReason").value("서류 미비"));

		Membership membership = loadMembershipPort.findByUuid(new MembershipUuid(flow.membershipUuid)).orElseThrow();
		assertThat(membership.status()).isEqualTo(MembershipStatus.REJECTED);
		assertThat(membership.rejectReason()).isEqualTo("서류 미비");
	}

	@Test
	@DisplayName("6. 승인 전 멤버십 가입 차단")
	void scenario06_joinBlockedBeforeApproval() throws Exception {
		Flow flow = applyPendingMembership();
		follow(flow.memberUuid, flow.creatorUuid);

		mockMvc.perform(post(API + "/memberships/subscriptions/validate")
						.header("X-Member-UUID", flow.memberUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content(creatorBody(flow.creatorUuid)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value(MembershipErrorCodes.MEMBERSHIP_NOT_APPROVED));

		mockMvc.perform(post(API + "/memberships/subscriptions/payments")
						.header("X-Member-UUID", flow.memberUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content(creatorBody(flow.creatorUuid)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value(MembershipErrorCodes.MEMBERSHIP_NOT_APPROVED));
	}

	@Test
	@DisplayName("7. 팔로우하지 않은 회원 가입 차단")
	void scenario07_nonFollowerCannotJoin() throws Exception {
		Flow flow = approveMembership();

		mockMvc.perform(post(API + "/memberships/subscriptions/validate")
						.header("X-Member-UUID", flow.memberUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content(creatorBody(flow.creatorUuid)))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value(MembershipErrorCodes.FOLLOW_REQUIRED))
				.andExpect(jsonPath("$.message").value("팔로워만 멤버십에 가입할 수 있습니다."));
	}

	@Test
	@DisplayName("8. 팔로우 회원 가입 허용")
	void scenario08_followerCanJoin() throws Exception {
		Flow flow = approveAndFollow();

		mockMvc.perform(post(API + "/memberships/subscriptions/validate")
						.header("X-Member-UUID", flow.memberUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content(creatorBody(flow.creatorUuid)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.eligible").value(true))
				.andExpect(jsonPath("$.following").value(true))
				.andExpect(jsonPath("$.membershipUuid").value(flow.membershipUuid.toString()))
				.andExpect(jsonPath("$.monthlyPrice").value(MONTHLY_PRICE))
				.andExpect(jsonPath("$.priceUnit").value(MembershipApplicationPolicy.PRICE_UNIT_TOKEN));
	}

	@Test
	@DisplayName("9. Token 부족 → Subscription 생성 안 됨")
	void scenario09_tokenFailureDoesNotCreateSubscription() throws Exception {
		Flow flow = approveAndFollow();
		StartedPayment started = startPayment(flow);
		assertThat(loadPaymentPort.findByUuid(new PaymentUuid(started.paymentUuid)).orElseThrow().paidAt()).isNull();

		tokenFailedConsumer.consume(tokenFailedPayload(
				UUID.randomUUID(),
				flow.memberUuid,
				started.paymentUuid,
				started.subscriptionUuid
		));

		assertThat(loadPaymentPort.findByUuid(new PaymentUuid(started.paymentUuid)).orElseThrow().paymentStatus())
				.isEqualTo(PaymentStatus.FAILED);
		assertThat(loadSubscriptionPort.findByUuid(new SubscriptionUuid(started.subscriptionUuid))).isEmpty();
		assertThat(loadRevenuePort.findByCreator(new CreatorUuid(flow.creatorUuid)).orElseThrow().totalRevenue()).isZero();
		assertThat(loadRevenueLedgerPort.findByPaymentUuid(new PaymentUuid(started.paymentUuid))).isEmpty();
	}

	@Test
	@DisplayName("10. Token 정상 차감 → Subscription ACTIVE")
	void scenario10_tokenSuccessActivatesSubscription() throws Exception {
		Flow flow = approveAndFollow();
		StartedPayment started = startPayment(flow);

		consumeTokenSucceeded(flow, started);

		assertThat(loadPaymentPort.findByUuid(new PaymentUuid(started.paymentUuid)).orElseThrow().paymentStatus())
				.isEqualTo(PaymentStatus.SUCCESS);
		assertThat(loadSubscriptionPort.findByUuid(new SubscriptionUuid(started.subscriptionUuid)).orElseThrow().status())
				.isEqualTo(SubscriptionStatus.ACTIVE);
		assertThat(outboxTypes(started.paymentUuid.toString())).contains(MembershipEventTypes.TOKEN_DEDUCTION_REQUESTED);
		assertThat(outboxTypes(started.subscriptionUuid.toString())).contains(MembershipEventTypes.MEMBERSHIP_SUBSCRIBED);
	}

	@Test
	@DisplayName("11. 지정 가격과 정확히 동일한 Token 차감")
	void scenario11_tokenAmountEqualsMembershipPrice() throws Exception {
		Flow flow = approveAndFollow();
		StartedPayment started = startPayment(flow);

		assertThat(started.amount).isEqualTo(MONTHLY_PRICE);
		assertThat(loadPaymentPort.findByUuid(new PaymentUuid(started.paymentUuid)).orElseThrow().amount())
				.isEqualTo(MONTHLY_PRICE);
		assertThat(loadMembershipPort.findByUuid(new MembershipUuid(flow.membershipUuid)).orElseThrow().monthlyPrice())
				.isEqualTo(MONTHLY_PRICE);
	}

	@Test
	@DisplayName("12. 1 Token = 100원 환산")
	void scenario12_oneTokenEqualsOneHundredWon() throws Exception {
		Flow flow = approveAndFollow();
		StartedPayment started = startPayment(flow);
		consumeTokenSucceeded(flow, started);

		MembershipRevenueLedger ledger = loadRevenueLedgerPort.findByPaymentUuid(new PaymentUuid(started.paymentUuid))
				.orElseThrow();
		assertThat(ledger.grossKrw()).isEqualTo(GROSS_KRW);
		assertThat(ledger.grossKrw()).isEqualTo(started.amount * RevenueSharePolicy.TOKEN_TO_KRW);
	}

	@Test
	@DisplayName("13. Creator 30% 수익 적립")
	void scenario13_creatorReceivesThirtyPercent() throws Exception {
		Flow flow = approveAndFollow();
		StartedPayment started = startPayment(flow);
		consumeTokenSucceeded(flow, started);

		MembershipRevenueLedger ledger = loadRevenueLedgerPort.findByPaymentUuid(new PaymentUuid(started.paymentUuid))
				.orElseThrow();
		MembershipRevenue revenue = loadRevenuePort.findByCreator(new CreatorUuid(flow.creatorUuid)).orElseThrow();
		assertThat(ledger.creatorShareKrw()).isEqualTo(CREATOR_SHARE_KRW);
		assertThat(revenue.availableRevenue()).isEqualTo(CREATOR_SHARE_KRW);
		assertThat(revenue.totalRevenue()).isEqualTo(CREATOR_SHARE_KRW);

		mockMvc.perform(get(API + "/memberships/me/revenue").header("X-Member-UUID", flow.creatorUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.availableRevenue").value(CREATOR_SHARE_KRW))
				.andExpect(jsonPath("$.totalRevenue").value(CREATOR_SHARE_KRW));
	}

	@Test
	@DisplayName("14. 회사 70% 계산 검증")
	void scenario14_companyReceivesSeventyPercent() throws Exception {
		Flow flow = approveAndFollow();
		StartedPayment started = startPayment(flow);
		consumeTokenSucceeded(flow, started);

		MembershipRevenueLedger ledger = loadRevenueLedgerPort.findByPaymentUuid(new PaymentUuid(started.paymentUuid))
				.orElseThrow();
		assertThat(ledger.companyShareKrw()).isEqualTo(COMPANY_SHARE_KRW);
		assertThat(ledger.companyShareKrw() + ledger.creatorShareKrw()).isEqualTo(ledger.grossKrw());
	}

	@Test
	@DisplayName("15. Redis Entitlement 생성")
	void scenario15_entitlementIsCreated() throws Exception {
		Flow flow = approveAndFollow();
		StartedPayment started = startPayment(flow);
		consumeTokenSucceeded(flow, started);

		mockMvc.perform(get(API + "/memberships/me/entitlement/" + flow.creatorUuid)
						.header("X-Member-UUID", flow.memberUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.allowed").value(true))
				.andExpect(jsonPath("$.status").value(SubscriptionStatus.ACTIVE.name()));

		assertThat(entitlementCacheAdapter.find(new MemberUuid(flow.memberUuid), new CreatorUuid(flow.creatorUuid)))
				.isPresent()
				.get()
				.satisfies(entitlement -> assertThat(entitlement.allowed()).isTrue());
	}

	@Test
	@DisplayName("16. Redis 장애 시 DB fallback")
	void scenario16_entitlementFallsBackToSubscriptionWhenCacheMisses() throws Exception {
		Flow flow = approveAndFollow();
		StartedPayment started = startPayment(flow);
		consumeTokenSucceeded(flow, started);
		entitlementCacheAdapter.clear();

		mockMvc.perform(get(API + "/memberships/me/entitlement/" + flow.creatorUuid)
						.header("X-Member-UUID", flow.memberUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.allowed").value(true))
				.andExpect(jsonPath("$.status").value(SubscriptionStatus.ACTIVE.name()));

		assertThat(loadSubscriptionPort.findCurrentByMemberAndCreator(
				new MemberUuid(flow.memberUuid),
				new CreatorUuid(flow.creatorUuid)
		).orElseThrow().status()).isEqualTo(SubscriptionStatus.ACTIVE);
		assertThat(entitlementCacheAdapter.find(new MemberUuid(flow.memberUuid), new CreatorUuid(flow.creatorUuid)))
				.isPresent();
	}

	@Test
	@DisplayName("17. 멤버십 해지 → INACTIVE")
	void scenario17_cancelMakesSubscriptionInactive() throws Exception {
		Flow flow = approveAndFollow();
		StartedPayment started = startPayment(flow);
		consumeTokenSucceeded(flow, started);

		mockMvc.perform(post(API + "/memberships/me/subscriptions/" + started.subscriptionUuid + "/cancel")
						.header("X-Member-UUID", flow.memberUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(SubscriptionStatus.INACTIVE.name()))
				.andExpect(jsonPath("$.endedAt").exists());

		assertThat(loadSubscriptionPort.findByUuid(new SubscriptionUuid(started.subscriptionUuid)).orElseThrow().status())
				.isEqualTo(SubscriptionStatus.INACTIVE);
		assertThat(outboxTypes(started.subscriptionUuid.toString())).contains(MembershipEventTypes.MEMBERSHIP_CANCELED);
	}

	@Test
	@DisplayName("18. 해지 → Entitlement 삭제")
	void scenario18_cancelRemovesEntitlement() throws Exception {
		Flow flow = approveAndFollow();
		StartedPayment started = startPayment(flow);
		consumeTokenSucceeded(flow, started);
		mockMvc.perform(get(API + "/memberships/me/entitlement/" + flow.creatorUuid)
						.header("X-Member-UUID", flow.memberUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.allowed").value(true));

		mockMvc.perform(post(API + "/memberships/me/subscriptions/" + started.subscriptionUuid + "/cancel")
						.header("X-Member-UUID", flow.memberUuid))
				.andExpect(status().isOk());

		assertThat(entitlementCacheAdapter.find(new MemberUuid(flow.memberUuid), new CreatorUuid(flow.creatorUuid)))
				.isEmpty();
		mockMvc.perform(get(API + "/memberships/me/entitlement/" + flow.creatorUuid)
						.header("X-Member-UUID", flow.memberUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.allowed").value(false));
	}

	@Test
	@DisplayName("19. 정산 가능 금액 초과 신청 실패")
	void scenario19_settlementAmountExceededFails() throws Exception {
		Flow flow = subscribeSuccessfully();

		mockMvc.perform(post(API + "/memberships/me/settlements")
						.header("X-Member-UUID", flow.creatorUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"settlementAmount":%s}
								""".formatted(CREATOR_SHARE_KRW + 1)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value(MembershipErrorCodes.SETTLEMENT_AMOUNT_EXCEEDED))
				.andExpect(jsonPath("$.message").value("정산 가능 금액을 초과할 수 없습니다."));
	}

	@Test
	@DisplayName("20. 정산 REQUESTED → APPROVED → PAID")
	void scenario20_settlementRequestedApprovedPaid() throws Exception {
		Flow flow = subscribeSuccessfully();
		long settlementAmount = 2_000L;

		MvcResult requested = mockMvc.perform(post(API + "/memberships/me/settlements")
						.header("X-Member-UUID", flow.creatorUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"settlementAmount":%s}
								""".formatted(settlementAmount)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value(SettlementStatus.REQUESTED.name()))
				.andExpect(jsonPath("$.settlementAmount").value(settlementAmount))
				.andExpect(jsonPath("$.availableRevenue").value(CREATOR_SHARE_KRW - settlementAmount))
				.andExpect(jsonPath("$.reservedRevenue").value(settlementAmount))
				.andReturn();
		UUID settlementUuid = uuid(requested, "settlementUuid");

		mockMvc.perform(post(API + "/admin/settlements/" + settlementUuid + "/approve")
						.header("X-Admin-UUID", flow.adminUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(SettlementStatus.APPROVED.name()));

		mockMvc.perform(post(API + "/admin/settlements/" + settlementUuid + "/pay")
						.header("X-Admin-UUID", flow.adminUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(SettlementStatus.PAID.name()))
				.andExpect(jsonPath("$.availableRevenue").value(CREATOR_SHARE_KRW - settlementAmount))
				.andExpect(jsonPath("$.reservedRevenue").value(0))
				.andExpect(jsonPath("$.settledRevenue").value(settlementAmount));

		MembershipSettlement settlement = loadSettlementPort.findByUuid(new SettlementUuid(settlementUuid)).orElseThrow();
		MembershipRevenue revenue = loadRevenuePort.findByCreator(new CreatorUuid(flow.creatorUuid)).orElseThrow();
		assertThat(settlement.settlementStatus()).isEqualTo(SettlementStatus.PAID);
		assertThat(revenue.availableRevenue()).isEqualTo(CREATOR_SHARE_KRW - settlementAmount);
		assertThat(revenue.reservedRevenue()).isZero();
		assertThat(revenue.settledRevenue()).isEqualTo(settlementAmount);
		assertThat(outboxTypes(settlementUuid.toString()))
				.contains(MembershipEventTypes.SETTLEMENT_REQUESTED, MembershipEventTypes.SETTLEMENT_COMPLETED);
	}

	@Test
	@DisplayName("21. 정산 거절 시 가용 수익 복구")
	void scenario21_rejectSettlementRestoresAvailableRevenue() throws Exception {
		Flow flow = subscribeSuccessfully();
		long settlementAmount = 2_000L;

		MvcResult requested = mockMvc.perform(post(API + "/memberships/me/settlements")
						.header("X-Member-UUID", flow.creatorUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"settlementAmount":%s}
								""".formatted(settlementAmount)))
				.andExpect(status().isCreated())
				.andReturn();
		UUID settlementUuid = uuid(requested, "settlementUuid");

		mockMvc.perform(post(API + "/admin/settlements/" + settlementUuid + "/reject")
						.header("X-Admin-UUID", flow.adminUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"rejectReason":"계좌 정보 오류"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(SettlementStatus.REJECTED.name()))
				.andExpect(jsonPath("$.availableRevenue").value(CREATOR_SHARE_KRW))
				.andExpect(jsonPath("$.reservedRevenue").value(0))
				.andExpect(jsonPath("$.rejectReason").value("계좌 정보 오류"));

		MembershipRevenue revenue = loadRevenuePort.findByCreator(new CreatorUuid(flow.creatorUuid)).orElseThrow();
		assertThat(revenue.availableRevenue()).isEqualTo(CREATOR_SHARE_KRW);
		assertThat(revenue.reservedRevenue()).isZero();
		assertThat(revenue.settledRevenue()).isZero();
	}

	@Test
	@DisplayName("22. Kafka 동일 Event 중복 수신 멱등 처리")
	void scenario22_duplicateKafkaEventIsIdempotent() throws Exception {
		Flow flow = approveAndFollow();
		StartedPayment started = startPayment(flow);
		UUID eventUuid = UUID.randomUUID();
		String payload = tokenSucceededPayload(eventUuid, flow, started);

		tokenSucceededConsumer.consume(payload);
		tokenSucceededConsumer.consume(payload);

		assertThat(loadSubscriptionPort.findByUuid(new SubscriptionUuid(started.subscriptionUuid)).orElseThrow().status())
				.isEqualTo(SubscriptionStatus.ACTIVE);
		assertThat(jdbcTemplate.queryForObject(
				"select count(*) from membership_subscription where subscription_uuid = ?",
				Long.class,
				started.subscriptionUuid.toString()
		)).isEqualTo(1L);
		assertThat(jdbcTemplate.queryForObject(
				"select count(*) from membership_revenue_ledger where payment_uuid = ?",
				Long.class,
				started.paymentUuid.toString()
		)).isEqualTo(1L);
		assertThat(loadRevenuePort.findByCreator(new CreatorUuid(flow.creatorUuid)).orElseThrow().availableRevenue())
				.isEqualTo(CREATOR_SHARE_KRW);
		assertThat(processedMembershipEventPort.existsByEventUuid(eventUuid)).isTrue();
		assertThat(processedMembershipEventPort.existsByPaymentUuidAndEventType(
				started.paymentUuid,
				MembershipEventTypes.TOKEN_DEDUCTION_SUCCEEDED
		)).isTrue();
		assertThat(loadPaymentPort.findByUuid(new PaymentUuid(started.paymentUuid)).orElseThrow().paidAt()).isNotNull();
	}

	private Flow applyPendingMembership() throws Exception {
		UUID creatorUuid = UUID.randomUUID();
		UUID memberUuid = UUID.randomUUID();
		UUID adminUuid = UUID.randomUUID();
		prepareExplorer(creatorUuid);
		MvcResult result = mockMvc.perform(post(API + "/memberships/applications")
						.header("X-Member-UUID", creatorUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content(applicationBody()))
				.andExpect(status().isCreated())
				.andReturn();
		return new Flow(creatorUuid, memberUuid, adminUuid, uuid(result, "membershipUuid"));
	}

	private Flow approveMembership() throws Exception {
		Flow flow = applyPendingMembership();
		mockMvc.perform(post(API + "/admin/memberships/" + flow.membershipUuid + "/approve")
						.header("X-Admin-UUID", flow.adminUuid))
				.andExpect(status().isOk());
		return flow;
	}

	private Flow approveAndFollow() throws Exception {
		Flow flow = approveMembership();
		follow(flow.memberUuid, flow.creatorUuid);
		return flow;
	}

	private Flow subscribeSuccessfully() throws Exception {
		Flow flow = approveAndFollow();
		StartedPayment started = startPayment(flow);
		consumeTokenSucceeded(flow, started);
		return flow;
	}

	private StartedPayment startPayment(Flow flow) throws Exception {
		MvcResult result = mockMvc.perform(post(API + "/memberships/subscriptions/payments")
						.header("X-Member-UUID", flow.memberUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content(creatorBody(flow.creatorUuid)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value(PaymentStatus.READY.name()))
				.andExpect(jsonPath("$.amount").value(MONTHLY_PRICE))
				.andExpect(jsonPath("$.priceUnit").value(MembershipApplicationPolicy.PRICE_UNIT_TOKEN))
				.andReturn();
		MembershipPayment payment = loadPaymentPort.findByUuid(new PaymentUuid(uuid(result, "paymentUuid"))).orElseThrow();
		assertThat(payment.paidAt()).isNull();
		return new StartedPayment(uuid(result, "paymentUuid"), uuid(result, "subscriptionUuid"), payment.amount());
	}

	private void consumeTokenSucceeded(Flow flow, StartedPayment started) {
		tokenSucceededConsumer.consume(tokenSucceededPayload(UUID.randomUUID(), flow, started));
	}

	private void prepareExplorer(UUID creatorUuid) {
		prepareGrade(creatorUuid, "EXPLORER", "탐험가", 4);
	}

	private void prepareGrade(UUID memberUuid, String gradeCode, String gradeName, int gradeLevel) {
		gradeQueryPort.put(new MemberGradeResult(
				new MemberUuid(memberUuid),
				gradeCode,
				gradeName,
				gradeLevel
		));
	}

	private void follow(UUID memberUuid, UUID creatorUuid) {
		followQueryPort.follow(new MemberUuid(memberUuid), new CreatorUuid(creatorUuid));
	}

	private List<String> outboxTypes(String aggregateUuid) {
		return jdbcTemplate.queryForList(
				"select event_type from membership_outbox where aggregate_uuid = ? order by occurred_at",
				String.class,
				aggregateUuid
		);
	}

	private UUID uuid(MvcResult result, String field) throws Exception {
		JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
		return UUID.fromString(body.get(field).asText());
	}

	private static String applicationBody() {
		return """
				{
				  "membershipName": "윤휘명의 여행 멤버십",
				  "description": "월간 멤버십",
				  "monthlyPrice": %s,
				  "priceUnit": "TOKEN"
				}
				""".formatted(MONTHLY_PRICE);
	}

	private static String creatorBody(UUID creatorUuid) {
		return """
				{"creatorUuid":"%s"}
				""".formatted(creatorUuid);
	}

	private static String tokenSucceededPayload(UUID eventUuid, Flow flow, StartedPayment started) {
		return """
				{
				  "eventUuid": "%s",
				  "memberUuid": "%s",
				  "creatorUuid": "%s",
				  "paymentUuid": "%s",
				  "subscriptionUuid": "%s",
				  "succeededAt": "2026-08-22T00:10:00Z"
				}
				""".formatted(
				eventUuid,
				flow.memberUuid,
				flow.creatorUuid,
				started.paymentUuid,
				started.subscriptionUuid
		);
	}

	private static String tokenFailedPayload(
			UUID eventUuid,
			UUID memberUuid,
			UUID paymentUuid,
			UUID subscriptionUuid
	) {
		return """
				{
				  "eventUuid": "%s",
				  "memberUuid": "%s",
				  "paymentUuid": "%s",
				  "subscriptionUuid": "%s",
				  "failedAt": "2026-08-22T00:10:00Z"
				}
				""".formatted(eventUuid, memberUuid, paymentUuid, subscriptionUuid);
	}

	private record Flow(UUID creatorUuid, UUID memberUuid, UUID adminUuid, UUID membershipUuid) {
	}

	private record StartedPayment(UUID paymentUuid, UUID subscriptionUuid, long amount) {
	}

	@TestConfiguration
	static class ExternalPortTestConfig {

		@Bean
		@Primary
		InMemoryGradeQueryAdapter inMemoryGradeQueryAdapter() {
			return new InMemoryGradeQueryAdapter();
		}

		@Bean
		@Primary
		InMemoryFollowQueryAdapter inMemoryFollowQueryAdapter() {
			return new InMemoryFollowQueryAdapter();
		}
	}
}
