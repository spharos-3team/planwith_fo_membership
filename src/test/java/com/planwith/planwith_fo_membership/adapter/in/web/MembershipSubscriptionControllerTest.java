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
import com.planwith.planwith_fo_membership.application.port.in.command.CancelSubscriptionUseCase;
import com.planwith.planwith_fo_membership.application.port.in.command.StartTokenPaymentUseCase;
import com.planwith.planwith_fo_membership.application.port.in.command.ValidateJoinEligibilityUseCase;
import com.planwith.planwith_fo_membership.application.query.CancelSubscriptionResult;
import com.planwith.planwith_fo_membership.application.query.StartTokenPaymentResult;
import com.planwith.planwith_fo_membership.application.query.ValidateJoinEligibilityResult;
import com.planwith.planwith_fo_membership.domain.exception.DuplicateSubscriptionException;
import com.planwith.planwith_fo_membership.domain.exception.FollowRequiredException;
import com.planwith.planwith_fo_membership.domain.exception.InvalidMembershipStateException;
import com.planwith.planwith_fo_membership.domain.exception.InvalidSubscriptionStateException;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.PaymentStatus;
import com.planwith.planwith_fo_membership.domain.model.SubscriptionStatus;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.PaymentUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;
import com.planwith.planwith_fo_membership.domain.service.MembershipApplicationPolicy;

class MembershipSubscriptionControllerTest {

	private MockMvc mockMvc;
	private ValidateJoinEligibilityUseCase validateJoinEligibilityUseCase;
	private StartTokenPaymentUseCase startTokenPaymentUseCase;
	private CancelSubscriptionUseCase cancelSubscriptionUseCase;

	@BeforeEach
	void setUp() {
		validateJoinEligibilityUseCase = Mockito.mock(ValidateJoinEligibilityUseCase.class);
		startTokenPaymentUseCase = Mockito.mock(StartTokenPaymentUseCase.class);
		cancelSubscriptionUseCase = Mockito.mock(CancelSubscriptionUseCase.class);
		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
		validator.afterPropertiesSet();
		mockMvc = MockMvcBuilders.standaloneSetup(
						new MembershipSubscriptionController(
								validateJoinEligibilityUseCase,
								startTokenPaymentUseCase,
								cancelSubscriptionUseCase
						)
				)
				.setControllerAdvice(new GlobalExceptionHandler())
				.setValidator(validator)
				.build();
	}

	@Test
	void validateJoinReturnsEligibleResult() throws Exception {
		when(validateJoinEligibilityUseCase.validate(any())).thenReturn(new ValidateJoinEligibilityResult(
				MemberUuid.from("11111111-1111-1111-1111-111111111111"),
				CreatorUuid.from("22222222-2222-2222-2222-222222222222"),
				MembershipUuid.from("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
				100,
				MembershipApplicationPolicy.PRICE_UNIT_TOKEN,
				true,
				true
		));

		mockMvc.perform(post("/api/planwith-fo-membership/memberships/subscriptions/validate")
						.header("X-Member-UUID", "11111111-1111-1111-1111-111111111111")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "creatorUuid": "22222222-2222-2222-2222-222222222222"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.eligible").value(true))
				.andExpect(jsonPath("$.following").value(true))
				.andExpect(jsonPath("$.membershipUuid").value("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
				.andExpect(jsonPath("$.monthlyPrice").value(100))
				.andExpect(jsonPath("$.priceUnit").value("TOKEN"));
	}

	@Test
	void returnsForbiddenWhenFollowIsRequired() throws Exception {
		when(validateJoinEligibilityUseCase.validate(any()))
				.thenThrow(new FollowRequiredException("팔로워만 멤버십에 가입할 수 있습니다."));

		mockMvc.perform(post("/api/planwith-fo-membership/memberships/subscriptions/validate")
						.header("X-Member-UUID", "11111111-1111-1111-1111-111111111111")
						.contentType(MediaType.APPLICATION_JSON)
						.content(validBody()))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("FOLLOW_REQUIRED"));
	}

	@Test
	void returnsConflictWhenAlreadySubscribed() throws Exception {
		when(validateJoinEligibilityUseCase.validate(any()))
				.thenThrow(new DuplicateSubscriptionException("이미 가입한 멤버십입니다."));

		mockMvc.perform(post("/api/planwith-fo-membership/memberships/subscriptions/validate")
						.header("X-Member-UUID", "11111111-1111-1111-1111-111111111111")
						.contentType(MediaType.APPLICATION_JSON)
						.content(validBody()))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("DUPLICATE_SUBSCRIPTION"));
	}

	@Test
	void returnsConflictWhenMembershipIsNotApproved() throws Exception {
		when(validateJoinEligibilityUseCase.validate(any()))
				.thenThrow(new InvalidMembershipStateException("승인된 멤버십만 가입할 수 있습니다."));

		mockMvc.perform(post("/api/planwith-fo-membership/memberships/subscriptions/validate")
						.header("X-Member-UUID", "11111111-1111-1111-1111-111111111111")
						.contentType(MediaType.APPLICATION_JSON)
						.content(validBody()))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("INVALID_MEMBERSHIP_STATE"));
	}

	@Test
	void startTokenPaymentReturnsReadyPayment() throws Exception {
		when(startTokenPaymentUseCase.start(any())).thenReturn(new StartTokenPaymentResult(
				PaymentUuid.from("cccccccc-cccc-cccc-cccc-cccccccccccc"),
				SubscriptionUuid.from("55555555-5555-5555-5555-555555555555"),
				100L,
				MembershipApplicationPolicy.PRICE_UNIT_TOKEN,
				PaymentStatus.READY
		));

		mockMvc.perform(post("/api/planwith-fo-membership/memberships/subscriptions/payments")
						.header("X-Member-UUID", "11111111-1111-1111-1111-111111111111")
						.contentType(MediaType.APPLICATION_JSON)
						.content(validBody()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.paymentUuid").value("cccccccc-cccc-cccc-cccc-cccccccccccc"))
				.andExpect(jsonPath("$.amount").value(100))
				.andExpect(jsonPath("$.priceUnit").value("TOKEN"))
				.andExpect(jsonPath("$.status").value("READY"));
	}

	@Test
	void startTokenPaymentReturnsForbiddenWhenFollowIsRequired() throws Exception {
		when(startTokenPaymentUseCase.start(any()))
				.thenThrow(new FollowRequiredException("팔로워만 멤버십에 가입할 수 있습니다."));

		mockMvc.perform(post("/api/planwith-fo-membership/memberships/subscriptions/payments")
						.header("X-Member-UUID", "11111111-1111-1111-1111-111111111111")
						.contentType(MediaType.APPLICATION_JSON)
						.content(validBody()))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("FOLLOW_REQUIRED"));
	}

	@Test
	void cancelSubscriptionReturnsInactiveResult() throws Exception {
		when(cancelSubscriptionUseCase.cancel(any())).thenReturn(new CancelSubscriptionResult(
				SubscriptionUuid.from("55555555-5555-5555-5555-555555555555"),
				SubscriptionStatus.INACTIVE,
				Instant.parse("2026-08-22T00:10:00Z")
		));

		mockMvc.perform(post("/api/planwith-fo-membership/memberships/me/subscriptions/55555555-5555-5555-5555-555555555555/cancel")
						.header("X-Member-UUID", "11111111-1111-1111-1111-111111111111"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.subscriptionUuid").value("55555555-5555-5555-5555-555555555555"))
				.andExpect(jsonPath("$.status").value("INACTIVE"))
				.andExpect(jsonPath("$.endedAt").exists());
	}

	@Test
	void cancelSubscriptionReturnsConflictWhenAlreadyInactive() throws Exception {
		when(cancelSubscriptionUseCase.cancel(any()))
				.thenThrow(new InvalidSubscriptionStateException("이미 해지되었거나 만료된 구독입니다."));

		mockMvc.perform(post("/api/planwith-fo-membership/memberships/me/subscriptions/55555555-5555-5555-5555-555555555555/cancel")
						.header("X-Member-UUID", "11111111-1111-1111-1111-111111111111"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("INVALID_SUBSCRIPTION_STATE"));
	}

	@Test
	void rejectsMissingCreatorUuid() throws Exception {
		mockMvc.perform(post("/api/planwith-fo-membership/memberships/subscriptions/validate")
						.header("X-Member-UUID", "11111111-1111-1111-1111-111111111111")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
	}

	private static String validBody() {
		return """
				{
				  "creatorUuid": "22222222-2222-2222-2222-222222222222"
				}
				""";
	}
}
