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
import com.planwith.planwith_fo_membership.application.port.in.command.ApplyMembershipApplicationUseCase;
import com.planwith.planwith_fo_membership.application.port.in.command.ValidateMembershipApplicationUseCase;
import com.planwith.planwith_fo_membership.application.query.ApplyMembershipApplicationResult;
import com.planwith.planwith_fo_membership.application.query.ValidateMembershipApplicationResult;
import com.planwith.planwith_fo_membership.domain.exception.InsufficientMembershipGradeException;
import com.planwith.planwith_fo_membership.domain.model.MembershipStatus;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.RevenueUuid;
import com.planwith.planwith_fo_membership.domain.service.MembershipApplicationPolicy;

class MembershipApplicationControllerTest {

	private MockMvc mockMvc;
	private ValidateMembershipApplicationUseCase validateMembershipApplicationUseCase;
	private ApplyMembershipApplicationUseCase applyMembershipApplicationUseCase;

	@BeforeEach
	void setUp() {
		validateMembershipApplicationUseCase = Mockito.mock(ValidateMembershipApplicationUseCase.class);
		applyMembershipApplicationUseCase = Mockito.mock(ApplyMembershipApplicationUseCase.class);
		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
		validator.afterPropertiesSet();
		mockMvc = MockMvcBuilders.standaloneSetup(
						new MembershipApplicationController(
								validateMembershipApplicationUseCase,
								applyMembershipApplicationUseCase
						)
				)
				.setControllerAdvice(new GlobalExceptionHandler())
				.setValidator(validator)
				.build();
	}

	@Test
	void validateApplicationReturnsEligibleResult() throws Exception {
		when(validateMembershipApplicationUseCase.validate(any())).thenReturn(
				new ValidateMembershipApplicationResult(
						CreatorUuid.from("22222222-2222-2222-2222-222222222222"),
						"EXPLORER",
						12900,
						MembershipApplicationPolicy.PRICE_UNIT_TOKEN,
						true
				)
		);

		mockMvc.perform(post("/api/planwith-fo-membership/memberships/applications/validate")
						.header("X-Auth-User-Id", "22222222-2222-2222-2222-222222222222")
						.contentType(MediaType.APPLICATION_JSON)
						.content(validBody()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.eligible").value(true))
				.andExpect(jsonPath("$.gradeCode").value("EXPLORER"))
				.andExpect(jsonPath("$.monthlyPrice").value(12900))
				.andExpect(jsonPath("$.priceUnit").value("TOKEN"));
	}

	@Test
	void applyApplicationCreatesPendingMembership() throws Exception {
		when(applyMembershipApplicationUseCase.apply(any())).thenReturn(
				new ApplyMembershipApplicationResult(
						MembershipUuid.from("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
						CreatorUuid.from("22222222-2222-2222-2222-222222222222"),
						"윤휘명의 여행 멤버십",
						100,
						MembershipApplicationPolicy.PRICE_UNIT_TOKEN,
						MembershipStatus.PENDING,
						RevenueUuid.from("66666666-6666-6666-6666-666666666666")
				)
		);

		mockMvc.perform(post("/api/planwith-fo-membership/memberships/applications")
						.header("X-Auth-User-Id", "22222222-2222-2222-2222-222222222222")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "membershipName": "윤휘명의 여행 멤버십",
								  "monthlyPrice": 100,
								  "priceUnit": "TOKEN"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.membershipUuid").value("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
				.andExpect(jsonPath("$.status").value("PENDING"))
				.andExpect(jsonPath("$.monthlyPrice").value(100))
				.andExpect(jsonPath("$.priceUnit").value("TOKEN"))
				.andExpect(jsonPath("$.revenueUuid").value("66666666-6666-6666-6666-666666666666"));
	}

	@Test
	void rejectsNonTokenPriceUnit() throws Exception {
		mockMvc.perform(post("/api/planwith-fo-membership/memberships/applications")
						.header("X-Auth-User-Id", "22222222-2222-2222-2222-222222222222")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "membershipName": "크리에이터 멤버십",
								  "monthlyPrice": 12900,
								  "priceUnit": "KRW"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
	}

	@Test
	void rejectsNonPositivePrice() throws Exception {
		mockMvc.perform(post("/api/planwith-fo-membership/memberships/applications/validate")
						.header("X-Auth-User-Id", "22222222-2222-2222-2222-222222222222")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "membershipName": "크리에이터 멤버십",
								  "monthlyPrice": 0,
								  "priceUnit": "TOKEN"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
	}

	@Test
	void returnsForbiddenWhenMemberHeaderIsMissing() throws Exception {
		mockMvc.perform(post("/api/planwith-fo-membership/memberships/applications/validate")
						.contentType(MediaType.APPLICATION_JSON)
						.content(validBody()))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("FORBIDDEN_CREATOR"));
	}

	@Test
	void returnsForbiddenWhenGradeIsBelowExplorer() throws Exception {
		when(validateMembershipApplicationUseCase.validate(any()))
				.thenThrow(new InsufficientMembershipGradeException("Explorer 이상 등급만 멤버십을 개설할 수 있습니다."));

		mockMvc.perform(post("/api/planwith-fo-membership/memberships/applications/validate")
						.header("X-Auth-User-Id", "22222222-2222-2222-2222-222222222222")
						.contentType(MediaType.APPLICATION_JSON)
						.content(validBody()))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("MEMBERSHIP_GRADE_NOT_ELIGIBLE"));
	}

	private static String validBody() {
		return """
				{
				  "membershipName": "크리에이터 멤버십",
				  "description": "월간 멤버십",
				  "monthlyPrice": 12900,
				  "priceUnit": "TOKEN"
				}
				""";
	}
}
