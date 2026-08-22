package com.planwith.planwith_fo_membership.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.planwith.planwith_fo_membership.adapter.in.web.exception.GlobalExceptionHandler;
import com.planwith.planwith_fo_membership.application.port.in.query.GetMyMembershipQueryUseCase;
import com.planwith.planwith_fo_membership.application.port.in.query.GetRevenueQueryUseCase;
import com.planwith.planwith_fo_membership.application.port.in.query.ListCreatorSubscribersQueryUseCase;
import com.planwith.planwith_fo_membership.application.port.in.query.ListJoinedMembershipsQueryUseCase;
import com.planwith.planwith_fo_membership.application.query.CreatorSubscriberResult;
import com.planwith.planwith_fo_membership.application.query.CreatorSubscribersResult;
import com.planwith.planwith_fo_membership.application.query.JoinedMembershipResult;
import com.planwith.planwith_fo_membership.application.query.MyMembershipResult;
import com.planwith.planwith_fo_membership.application.query.RevenueResult;
import com.planwith.planwith_fo_membership.domain.model.MembershipStatus;
import com.planwith.planwith_fo_membership.domain.model.SubscriptionStatus;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.RevenueUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;
import com.planwith.planwith_fo_membership.domain.service.MembershipApplicationPolicy;

class MembershipManagementQueryControllerTest {

	private static final String MEMBER_UUID = "22222222-2222-2222-2222-222222222222";

	private MockMvc mockMvc;
	private GetMyMembershipQueryUseCase getMyMembershipQueryUseCase;
	private ListJoinedMembershipsQueryUseCase listJoinedMembershipsQueryUseCase;
	private ListCreatorSubscribersQueryUseCase listCreatorSubscribersQueryUseCase;
	private GetRevenueQueryUseCase getRevenueQueryUseCase;

	@BeforeEach
	void setUp() {
		getMyMembershipQueryUseCase = Mockito.mock(GetMyMembershipQueryUseCase.class);
		listJoinedMembershipsQueryUseCase = Mockito.mock(ListJoinedMembershipsQueryUseCase.class);
		listCreatorSubscribersQueryUseCase = Mockito.mock(ListCreatorSubscribersQueryUseCase.class);
		getRevenueQueryUseCase = Mockito.mock(GetRevenueQueryUseCase.class);
		mockMvc = MockMvcBuilders.standaloneSetup(
						new MembershipManagementQueryController(
								getMyMembershipQueryUseCase,
								listJoinedMembershipsQueryUseCase,
								listCreatorSubscribersQueryUseCase,
								getRevenueQueryUseCase
						)
				)
				.setControllerAdvice(new GlobalExceptionHandler())
				.build();
	}

	@Test
	void getMyMembershipReturnsEmptyStateWhenNotOpened() throws Exception {
		when(getMyMembershipQueryUseCase.get(any())).thenReturn(Optional.empty());

		mockMvc.perform(get("/api/planwith-fo-membership/memberships/me")
						.header("X-Member-UUID", MEMBER_UUID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.hasMembership").value(false))
				.andExpect(jsonPath("$.membershipUuid").doesNotExist())
				.andExpect(jsonPath("$.monthlyPrice").doesNotExist());
	}

	@Test
	void getMyMembershipReturnsStatusAndPrice() throws Exception {
		when(getMyMembershipQueryUseCase.get(any())).thenReturn(Optional.of(new MyMembershipResult(
				MembershipUuid.from("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
				CreatorUuid.from(MEMBER_UUID),
				"윤휘명의 여행 멤버십",
				100,
				MembershipApplicationPolicy.PRICE_UNIT_TOKEN,
				MembershipStatus.APPROVED,
				null
		)));

		mockMvc.perform(get("/api/planwith-fo-membership/memberships/me")
						.header("X-Member-UUID", MEMBER_UUID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.hasMembership").value(true))
				.andExpect(jsonPath("$.membershipUuid").value("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
				.andExpect(jsonPath("$.membershipName").value("윤휘명의 여행 멤버십"))
				.andExpect(jsonPath("$.monthlyPrice").value(100))
				.andExpect(jsonPath("$.priceUnit").value("TOKEN"))
				.andExpect(jsonPath("$.status").value("APPROVED"));
	}

	@Test
	void listJoinedMembershipsReturnsSubscriptions() throws Exception {
		when(listJoinedMembershipsQueryUseCase.list(any())).thenReturn(List.of(new JoinedMembershipResult(
				SubscriptionUuid.from("55555555-5555-5555-5555-555555555555"),
				MembershipUuid.from("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
				CreatorUuid.from("33333333-3333-3333-3333-333333333333"),
				"탐험가 멤버십",
				200,
				MembershipApplicationPolicy.PRICE_UNIT_TOKEN,
				SubscriptionStatus.ACTIVE,
				Instant.parse("2026-01-15T00:00:00Z"),
				null
		)));

		mockMvc.perform(get("/api/planwith-fo-membership/memberships/me/subscriptions")
						.header("X-Member-UUID", MEMBER_UUID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].subscriptionUuid").value("55555555-5555-5555-5555-555555555555"))
				.andExpect(jsonPath("$[0].membershipName").value("탐험가 멤버십"))
				.andExpect(jsonPath("$[0].monthlyPrice").value(200))
				.andExpect(jsonPath("$[0].priceUnit").value("TOKEN"))
				.andExpect(jsonPath("$[0].status").value("ACTIVE"))
				.andExpect(jsonPath("$[0].startedAt").exists())
				.andExpect(jsonPath("$[0].endedAt").doesNotExist());
	}

	@Test
	void listMySubscribersReturnsCountAndMembers() throws Exception {
		when(listCreatorSubscribersQueryUseCase.list(any())).thenReturn(new CreatorSubscribersResult(
				1,
				List.of(new CreatorSubscriberResult(
						SubscriptionUuid.from("55555555-5555-5555-5555-555555555555"),
						MemberUuid.from("11111111-1111-1111-1111-111111111111"),
						SubscriptionStatus.ACTIVE,
						Instant.parse("2026-01-15T00:00:00Z"),
						null
				))
		));

		mockMvc.perform(get("/api/planwith-fo-membership/memberships/me/subscribers")
						.header("X-Member-UUID", MEMBER_UUID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.subscriberCount").value(1))
				.andExpect(jsonPath("$.subscribers[0].memberUuid").value("11111111-1111-1111-1111-111111111111"))
				.andExpect(jsonPath("$.subscribers[0].status").value("ACTIVE"))
				.andExpect(jsonPath("$.subscribers[0].startedAt").exists());
	}

	@Test
	void getMyRevenueReturnsCreatorScreenAmounts() throws Exception {
		when(getRevenueQueryUseCase.get(any())).thenReturn(new RevenueResult(
				RevenueUuid.from("66666666-6666-6666-6666-666666666666"),
				CreatorUuid.from(MEMBER_UUID),
				120_000L,
				70_000L,
				50_000L
		));

		mockMvc.perform(get("/api/planwith-fo-membership/memberships/me/revenue")
						.header("X-Member-UUID", MEMBER_UUID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.revenueUuid").value("66666666-6666-6666-6666-666666666666"))
				.andExpect(jsonPath("$.totalRevenue").value(120000))
				.andExpect(jsonPath("$.availableRevenue").value(70000))
				.andExpect(jsonPath("$.settledRevenue").value(50000));
	}
}
