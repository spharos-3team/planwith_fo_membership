package com.planwith.planwith_fo_membership.adapter.in.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.planwith.planwith_fo_membership.adapter.in.web.exception.GlobalExceptionHandler;
import com.planwith.planwith_fo_membership.application.port.in.query.CountActiveSubscribersQueryUseCase;
import com.planwith.planwith_fo_membership.application.query.CreatorSubscriberCountResult;

class CreatorSubscriberCountQueryControllerTest {

	private static final UUID CREATOR_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");

	private MockMvc mockMvc;
	private CountActiveSubscribersQueryUseCase countActiveSubscribersQueryUseCase;

	@BeforeEach
	void setUp() {
		countActiveSubscribersQueryUseCase = Mockito.mock(CountActiveSubscribersQueryUseCase.class);
		mockMvc = MockMvcBuilders.standaloneSetup(
						new CreatorSubscriberCountQueryController(countActiveSubscribersQueryUseCase)
				)
				.setControllerAdvice(new GlobalExceptionHandler())
				.build();
	}

	@Test
	void returnsSubscriberCountsWithoutAuthHeader() throws Exception {
		when(countActiveSubscribersQueryUseCase.count(List.of(CREATOR_UUID)))
				.thenReturn(List.of(new CreatorSubscriberCountResult(CREATOR_UUID, 12L)));

		mockMvc.perform(get("/api/planwith-fo-membership/memberships/creators/subscriber-counts")
						.param("creatorUuids", CREATOR_UUID.toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[0].creatorUuid").value(CREATOR_UUID.toString()))
				.andExpect(jsonPath("$.items[0].subscriberCount").value(12));
	}

	@Test
	void returnsEmptyItemsWhenCreatorUuidsMissing() throws Exception {
		when(countActiveSubscribersQueryUseCase.count(List.of())).thenReturn(List.of());

		mockMvc.perform(get("/api/planwith-fo-membership/memberships/creators/subscriber-counts"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items.length()").value(0));
	}
}
