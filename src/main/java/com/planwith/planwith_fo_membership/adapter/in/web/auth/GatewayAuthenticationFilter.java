package com.planwith.planwith_fo_membership.adapter.in.web.auth;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class GatewayAuthenticationFilter extends OncePerRequestFilter {

	static final String AUTH_USER_ID = "X-Auth-User-Id";
	static final String AUTH_ROLES = "X-Auth-Roles";

	private static final String API_PREFIX = "/api/planwith-fo-membership";

	private final boolean enabled;

	public GatewayAuthenticationFilter(
			@Value("${membership.gateway-auth.enabled:true}") boolean enabled
	) {
		this.enabled = enabled;
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {
		if (!enabled || !requiresAuthentication(request.getRequestURI())) {
			filterChain.doFilter(request, response);
			return;
		}

		String memberUuid = request.getHeader(AUTH_USER_ID);
		if (memberUuid == null || memberUuid.isBlank()) {
			log.warn("GatewayAuthenticationFilter : doFilterInternal : 인증 회원 식별자 누락");
			writeError(response, HttpStatus.UNAUTHORIZED, "AUTHENTICATION_REQUIRED", "인증이 필요합니다.");
			return;
		}

		boolean adminRequest = request.getRequestURI().startsWith(API_PREFIX + "/admin/");
		if (adminRequest && !hasAdminRole(request.getHeader(AUTH_ROLES))) {
			log.warn("GatewayAuthenticationFilter : doFilterInternal : 관리자 권한 없는 요청 차단 - memberUuid={}", memberUuid);
			writeError(response, HttpStatus.FORBIDDEN, "FORBIDDEN_ADMIN", "관리자 권한이 필요합니다.");
			return;
		}

		filterChain.doFilter(request, response);
	}

	private static boolean requiresAuthentication(String requestUri) {
		return requestUri.startsWith(API_PREFIX)
				&& !requestUri.equals(API_PREFIX + "/deploy-check")
				&& !requestUri.equals(API_PREFIX + "/login")
				&& !requestUri.equals(API_PREFIX + "/memberships/creators/subscriber-counts");
	}

	private static boolean hasAdminRole(String roles) {
		if (roles == null || roles.isBlank()) {
			return false;
		}
		for (String role : roles.split(",")) {
			String normalized = role.trim();
			if ("ADMIN".equalsIgnoreCase(normalized) || "ROLE_ADMIN".equalsIgnoreCase(normalized)) {
				return true;
			}
		}
		return false;
	}

	private static void writeError(
			HttpServletResponse response,
			HttpStatus status,
			String code,
			String message
	) throws IOException {
		response.setStatus(status.value());
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write("{\"code\":\"" + code + "\",\"message\":\"" + message + "\"}");
	}
}
