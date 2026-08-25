package com.planwith.planwith_fo_membership.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.planwith.planwith_fo_membership.adapter.in.web.exception.GlobalExceptionHandler;
import com.planwith.planwith_fo_membership.application.port.in.query.CheckContentAccessQueryUseCase;
import com.planwith.planwith_fo_membership.application.query.ContentAccessResult;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;

class MembershipEntitlementControllerTest {

	private MockMvc mockMvc;
	private CheckContentAccessQueryUseCase checkContentAccessQueryUseCase;

	@BeforeEach
	void setUp() {
		checkContentAccessQueryUseCase = Mockito.mock(CheckContentAccessQueryUseCase.class);
		mockMvc = MockMvcBuilders.standaloneSetup(new MembershipEntitlementController(checkContentAccessQueryUseCase))
				.setControllerAdvice(new GlobalExceptionHandler())
				.build();
	}

	@Test
	void getMyEntitlementReturnsAllowedActive() throws Exception {
		when(checkContentAccessQueryUseCase.check(any())).thenReturn(new ContentAccessResult(
				MemberUuid.from("11111111-1111-1111-1111-111111111111"),
				CreatorUuid.from("22222222-2222-2222-2222-222222222222"),
				true,
				"ACTIVE"
		));

		mockMvc.perform(get("/api/planwith-fo-membership/memberships/me/entitlement/{creatorUuid}",
						"22222222-2222-2222-2222-222222222222")
						.header("X-Auth-User-Id", "11111111-1111-1111-1111-111111111111"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.memberUuid").value("11111111-1111-1111-1111-111111111111"))
				.andExpect(jsonPath("$.creatorUuid").value("22222222-2222-2222-2222-222222222222"))
				.andExpect(jsonPath("$.allowed").value(true))
				.andExpect(jsonPath("$.status").value("ACTIVE"));
	}

	@Test
	void getMyEntitlementReturnsDenied() throws Exception {
		when(checkContentAccessQueryUseCase.check(any())).thenReturn(new ContentAccessResult(
				MemberUuid.from("11111111-1111-1111-1111-111111111111"),
				CreatorUuid.from("22222222-2222-2222-2222-222222222222"),
				false,
				null
		));

		mockMvc.perform(get("/api/planwith-fo-membership/memberships/me/entitlement/{creatorUuid}",
						"22222222-2222-2222-2222-222222222222")
						.header("X-Auth-User-Id", "11111111-1111-1111-1111-111111111111"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.allowed").value(false));
	}
}
