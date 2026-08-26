package com.planwith.planwith_fo_membership;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class OpenApiServersIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void apiDocsPublishesGatewayRelativeServer() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.servers[0].url").value("/"))
				.andExpect(jsonPath("$.servers[0].description").value("API Gateway"));
	}

	@Test
	void apiDocsPublishesGatewayAndDirectAuthSchemes() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.components.securitySchemes.Bearer.type").value("http"))
				.andExpect(jsonPath("$.components.securitySchemes.Bearer.scheme").value("bearer"))
				.andExpect(jsonPath("$.components.securitySchemes.Bearer.bearerFormat").value("JWT"))
				.andExpect(jsonPath("$.components.securitySchemes['X-Auth-User-Id'].type").value("apiKey"))
				.andExpect(jsonPath("$.components.securitySchemes['X-Auth-User-Id'].in").value("header"))
				.andExpect(jsonPath("$.security[0].Bearer").exists())
				.andExpect(jsonPath("$.security[1]['X-Auth-User-Id']").exists());
	}

	@Test
	void apiDocsHidesGatewayIdentityHeaderParameters() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath(
						"$.paths['/api/planwith-fo-membership/memberships/me'].get.parameters[?(@.name=='X-Auth-User-Id')]"
				).isEmpty());
	}
}
