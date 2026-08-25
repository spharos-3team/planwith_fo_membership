package com.planwith.planwith_fo_membership.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.planwith.planwith_fo_membership.adapter.in.web.exception.GlobalExceptionHandler;
import com.planwith.planwith_fo_membership.application.port.in.command.ApproveSettlementUseCase;
import com.planwith.planwith_fo_membership.application.port.in.command.PaySettlementUseCase;
import com.planwith.planwith_fo_membership.application.port.in.command.RejectSettlementUseCase;
import com.planwith.planwith_fo_membership.application.query.ProcessSettlementResult;
import com.planwith.planwith_fo_membership.domain.exception.SettlementAlreadyProcessedException;
import com.planwith.planwith_fo_membership.domain.model.SettlementStatus;
import com.planwith.planwith_fo_membership.domain.model.vo.AdminUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SettlementUuid;

class SettlementAdminControllerTest {

	private static final String SETTLEMENT_UUID = "77777777-7777-7777-7777-777777777777";
	private static final String ADMIN_UUID = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb";

	private MockMvc mockMvc;
	private ApproveSettlementUseCase approveSettlementUseCase;
	private RejectSettlementUseCase rejectSettlementUseCase;
	private PaySettlementUseCase paySettlementUseCase;

	@BeforeEach
	void setUp() {
		approveSettlementUseCase = Mockito.mock(ApproveSettlementUseCase.class);
		rejectSettlementUseCase = Mockito.mock(RejectSettlementUseCase.class);
		paySettlementUseCase = Mockito.mock(PaySettlementUseCase.class);
		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
		validator.afterPropertiesSet();
		mockMvc = MockMvcBuilders.standaloneSetup(
						new SettlementAdminController(
								approveSettlementUseCase,
								rejectSettlementUseCase,
								paySettlementUseCase
						)
				)
				.setControllerAdvice(new GlobalExceptionHandler())
				.setValidator(validator)
				.build();
	}

	@Test
	void approveSettlementReturnsApprovedResult() throws Exception {
		when(approveSettlementUseCase.approve(any())).thenReturn(result(SettlementStatus.APPROVED, 20_000L, 30_000L, 0L, null));

		mockMvc.perform(post("/api/planwith-fo-membership/admin/settlements/{settlementUuid}/approve", SETTLEMENT_UUID)
						.header("X-Auth-User-Id", ADMIN_UUID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("APPROVED"))
				.andExpect(jsonPath("$.availableRevenue").value(20000))
				.andExpect(jsonPath("$.reservedRevenue").value(30000))
				.andExpect(jsonPath("$.settledRevenue").value(0));
	}

	@Test
	void rejectSettlementReturnsRejectedResult() throws Exception {
		when(rejectSettlementUseCase.reject(any())).thenReturn(result(SettlementStatus.REJECTED, 50_000L, 0L, 0L, "계좌 정보 오류"));

		mockMvc.perform(post("/api/planwith-fo-membership/admin/settlements/{settlementUuid}/reject", SETTLEMENT_UUID)
						.header("X-Auth-User-Id", ADMIN_UUID)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "rejectReason": "계좌 정보 오류"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("REJECTED"))
				.andExpect(jsonPath("$.availableRevenue").value(50000))
				.andExpect(jsonPath("$.rejectReason").value("계좌 정보 오류"));
	}

	@Test
	void paySettlementReturnsPaidResult() throws Exception {
		when(paySettlementUseCase.pay(any())).thenReturn(result(SettlementStatus.PAID, 20_000L, 0L, 30_000L, null));

		mockMvc.perform(post("/api/planwith-fo-membership/admin/settlements/{settlementUuid}/pay", SETTLEMENT_UUID)
						.header("X-Auth-User-Id", ADMIN_UUID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("PAID"))
				.andExpect(jsonPath("$.availableRevenue").value(20000))
				.andExpect(jsonPath("$.settledRevenue").value(30000));
	}

	@Test
	void alreadyProcessedSettlementReturnsConflict() throws Exception {
		when(approveSettlementUseCase.approve(any()))
				.thenThrow(new SettlementAlreadyProcessedException("신청된 정산만 승인할 수 있습니다."));

		mockMvc.perform(post("/api/planwith-fo-membership/admin/settlements/{settlementUuid}/approve", SETTLEMENT_UUID)
						.header("X-Auth-User-Id", ADMIN_UUID))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("SETTLEMENT_ALREADY_PROCESSED"));
	}

	@Test
	void returnsForbiddenWhenAdminHeaderIsMissing() throws Exception {
		mockMvc.perform(post("/api/planwith-fo-membership/admin/settlements/{settlementUuid}/approve", SETTLEMENT_UUID))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("FORBIDDEN_ADMIN"));
	}

	@Test
	void rejectRequiresReason() throws Exception {
		mockMvc.perform(post("/api/planwith-fo-membership/admin/settlements/{settlementUuid}/reject", SETTLEMENT_UUID)
						.header("X-Auth-User-Id", ADMIN_UUID)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "rejectReason": ""
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
	}

	private static ProcessSettlementResult result(
			SettlementStatus status,
			long availableRevenue,
			long reservedRevenue,
			long settledRevenue,
			String rejectReason
	) {
		return new ProcessSettlementResult(
				SettlementUuid.from(SETTLEMENT_UUID),
				CreatorUuid.from("22222222-2222-2222-2222-222222222222"),
				AdminUuid.from(ADMIN_UUID),
				status,
				30_000L,
				availableRevenue,
				reservedRevenue,
				settledRevenue,
				Instant.parse("2026-08-22T01:10:00Z"),
				status == SettlementStatus.PAID ? Instant.parse("2026-08-22T01:20:00Z") : null,
				rejectReason
		);
	}
}
