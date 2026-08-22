package com.planwith.planwith_fo_membership.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.planwith.planwith_fo_membership.adapter.in.web.exception.GlobalExceptionHandler;
import com.planwith.planwith_fo_membership.application.port.in.command.ApproveMembershipUseCase;
import com.planwith.planwith_fo_membership.application.port.in.command.RejectMembershipUseCase;
import com.planwith.planwith_fo_membership.application.query.ReviewMembershipResult;
import com.planwith.planwith_fo_membership.domain.exception.InvalidMembershipStateException;
import com.planwith.planwith_fo_membership.domain.model.MembershipStatus;
import com.planwith.planwith_fo_membership.domain.model.vo.AdminUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;

class MembershipAdminControllerTest {

	private MockMvc mockMvc;
	private ApproveMembershipUseCase approveMembershipUseCase;
	private RejectMembershipUseCase rejectMembershipUseCase;

	@BeforeEach
	void setUp() {
		approveMembershipUseCase = Mockito.mock(ApproveMembershipUseCase.class);
		rejectMembershipUseCase = Mockito.mock(RejectMembershipUseCase.class);
		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
		validator.afterPropertiesSet();
		mockMvc = MockMvcBuilders.standaloneSetup(
						new MembershipAdminController(approveMembershipUseCase, rejectMembershipUseCase)
				)
				.setControllerAdvice(new GlobalExceptionHandler())
				.setValidator(validator)
				.build();
	}

	@Test
	void approveMembershipReturnsApprovedResult() throws Exception {
		when(approveMembershipUseCase.approve(any())).thenReturn(result(MembershipStatus.APPROVED, null));

		mockMvc.perform(post("/api/planwith-fo-membership/admin/memberships/{membershipUuid}/approve",
						"aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
						.header("X-Admin-UUID", "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("APPROVED"))
				.andExpect(jsonPath("$.adminUuid").value("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"));
	}

	@Test
	void rejectMembershipReturnsRejectedResult() throws Exception {
		when(rejectMembershipUseCase.reject(any())).thenReturn(result(MembershipStatus.REJECTED, "서류 미비"));

		mockMvc.perform(post("/api/planwith-fo-membership/admin/memberships/{membershipUuid}/reject",
						"aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
						.header("X-Admin-UUID", "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "rejectReason": "서류 미비"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("REJECTED"))
				.andExpect(jsonPath("$.rejectReason").value("서류 미비"));
	}

	@Test
	void rejectRequiresReason() throws Exception {
		mockMvc.perform(post("/api/planwith-fo-membership/admin/memberships/{membershipUuid}/reject",
						"aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
						.header("X-Admin-UUID", "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "rejectReason": ""
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
	}

	@Test
	void alreadyProcessedMembershipReturnsConflict() throws Exception {
		when(approveMembershipUseCase.approve(any()))
				.thenThrow(new InvalidMembershipStateException("대기 중인 멤버십만 승인할 수 있습니다."));

		mockMvc.perform(post("/api/planwith-fo-membership/admin/memberships/{membershipUuid}/approve",
						"aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
						.header("X-Admin-UUID", "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("INVALID_MEMBERSHIP_STATE"));
	}

	private static ReviewMembershipResult result(MembershipStatus status, String rejectReason) {
		return new ReviewMembershipResult(
				MembershipUuid.from("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
				CreatorUuid.from("22222222-2222-2222-2222-222222222222"),
				status,
				AdminUuid.from("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
				rejectReason
		);
	}
}
