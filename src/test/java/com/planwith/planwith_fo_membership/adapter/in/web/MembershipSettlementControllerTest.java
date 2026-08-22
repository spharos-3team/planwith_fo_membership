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
import com.planwith.planwith_fo_membership.application.port.in.command.RequestSettlementUseCase;
import com.planwith.planwith_fo_membership.application.query.RequestSettlementResult;
import com.planwith.planwith_fo_membership.domain.exception.SettlementAmountExceededException;
import com.planwith.planwith_fo_membership.domain.model.SettlementStatus;
import com.planwith.planwith_fo_membership.domain.model.vo.SettlementUuid;

class MembershipSettlementControllerTest {

	private MockMvc mockMvc;
	private RequestSettlementUseCase requestSettlementUseCase;

	@BeforeEach
	void setUp() {
		requestSettlementUseCase = Mockito.mock(RequestSettlementUseCase.class);
		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
		validator.afterPropertiesSet();
		mockMvc = MockMvcBuilders.standaloneSetup(new MembershipSettlementController(requestSettlementUseCase))
				.setControllerAdvice(new GlobalExceptionHandler())
				.setValidator(validator)
				.build();
	}

	@Test
	void requestSettlementReturnsCreatedRequestedSettlement() throws Exception {
		when(requestSettlementUseCase.request(any())).thenReturn(new RequestSettlementResult(
				SettlementUuid.from("77777777-7777-7777-7777-777777777777"),
				SettlementStatus.REQUESTED,
				30_000L,
				20_000L,
				30_000L,
				Instant.parse("2026-08-22T01:00:00Z")
		));

		mockMvc.perform(post("/api/planwith-fo-membership/memberships/me/settlements")
						.header("X-Member-UUID", "22222222-2222-2222-2222-222222222222")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "settlementAmount": 30000
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.settlementUuid").value("77777777-7777-7777-7777-777777777777"))
				.andExpect(jsonPath("$.status").value("REQUESTED"))
				.andExpect(jsonPath("$.settlementAmount").value(30000))
				.andExpect(jsonPath("$.availableRevenue").value(20000))
				.andExpect(jsonPath("$.reservedRevenue").value(30000));
	}

	@Test
	void requestSettlementReturnsConflictWhenAmountExceedsAvailable() throws Exception {
		when(requestSettlementUseCase.request(any()))
				.thenThrow(new SettlementAmountExceededException("정산 가능 금액을 초과할 수 없습니다."));

		mockMvc.perform(post("/api/planwith-fo-membership/memberships/me/settlements")
						.header("X-Member-UUID", "22222222-2222-2222-2222-222222222222")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "settlementAmount": 30000
								}
								"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("SETTLEMENT_AMOUNT_EXCEEDED"));
	}

	@Test
	void rejectsNonPositiveSettlementAmount() throws Exception {
		mockMvc.perform(post("/api/planwith-fo-membership/memberships/me/settlements")
						.header("X-Member-UUID", "22222222-2222-2222-2222-222222222222")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "settlementAmount": 0
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
	}
}
