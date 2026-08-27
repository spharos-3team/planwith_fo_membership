package com.planwith.planwith_fo_membership.adapter.in.web.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class GatewayAuthenticationFilterTest {

	private final GatewayAuthenticationFilter filter = new GatewayAuthenticationFilter(true);

	@Test
	void allowsAuthenticatedMemberRequest() throws Exception {
		MockHttpServletRequest request = request("/api/planwith-fo-membership/memberships/me");
		request.addHeader(GatewayAuthenticationFilter.AUTH_USER_ID, "11111111-1111-1111-1111-111111111111");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, new MockFilterChain());

		assertThat(response.getStatus()).isEqualTo(200);
	}

	@Test
	void allowsUnauthenticatedSubscriberCountQuery() throws Exception {
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(
				request("/api/planwith-fo-membership/memberships/creators/subscriber-counts"),
				response,
				new MockFilterChain()
		);

		assertThat(response.getStatus()).isEqualTo(200);
	}

	@Test
	void rejectsUnauthenticatedMembershipRequest() throws Exception {
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request("/api/planwith-fo-membership/memberships/me"), response, new MockFilterChain());

		assertThat(response.getStatus()).isEqualTo(401);
	}

	@Test
	void rejectsAdminRequestWithoutAdminRole() throws Exception {
		MockHttpServletRequest request = request("/api/planwith-fo-membership/admin/memberships/id/approve");
		request.addHeader(GatewayAuthenticationFilter.AUTH_USER_ID, "11111111-1111-1111-1111-111111111111");
		request.addHeader(GatewayAuthenticationFilter.AUTH_ROLES, "ROLE_USER");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, new MockFilterChain());

		assertThat(response.getStatus()).isEqualTo(403);
		assertThat(response.getContentAsString()).contains("FORBIDDEN_ADMIN");
	}

	@Test
	void allowsAdminRequestWithAdminRole() throws Exception {
		MockHttpServletRequest request = request("/api/planwith-fo-membership/admin/memberships/id/approve");
		request.addHeader(GatewayAuthenticationFilter.AUTH_USER_ID, "11111111-1111-1111-1111-111111111111");
		request.addHeader(GatewayAuthenticationFilter.AUTH_ROLES, "ROLE_USER,ROLE_ADMIN");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, new MockFilterChain());

		assertThat(response.getStatus()).isEqualTo(200);
	}

	private static MockHttpServletRequest request(String uri) {
		return new MockHttpServletRequest("POST", uri);
	}
}
