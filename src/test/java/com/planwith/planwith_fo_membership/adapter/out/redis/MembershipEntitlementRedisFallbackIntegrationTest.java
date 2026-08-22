package com.planwith.planwith_fo_membership.adapter.out.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_membership.application.port.in.query.CheckContentAccessQueryUseCase;
import com.planwith.planwith_fo_membership.application.port.out.EntitlementCachePort;
import com.planwith.planwith_fo_membership.application.port.out.SaveMembershipPort;
import com.planwith.planwith_fo_membership.application.port.out.SaveSubscriptionPort;
import com.planwith.planwith_fo_membership.application.query.CheckContentAccessQuery;
import com.planwith.planwith_fo_membership.application.query.ContentAccessResult;
import com.planwith.planwith_fo_membership.config.MembershipCacheProperties;
import com.planwith.planwith_fo_membership.domain.model.Membership;
import com.planwith.planwith_fo_membership.domain.model.MembershipSubscription;
import com.planwith.planwith_fo_membership.domain.model.vo.AdminUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.SubscriptionUuid;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(MembershipEntitlementRedisFallbackIntegrationTest.FailingRedisCacheConfig.class)
@DisplayName("16. Redis 장애 시 DB fallback")
class MembershipEntitlementRedisFallbackIntegrationTest {

	@Autowired
	private CheckContentAccessQueryUseCase checkContentAccessQueryUseCase;

	@Autowired
	private SaveMembershipPort saveMembershipPort;

	@Autowired
	private SaveSubscriptionPort saveSubscriptionPort;

	@Autowired
	private EntitlementCachePort entitlementCachePort;

	@Test
	@DisplayName("Redis 조회/저장이 실패해도 활성 구독이면 접근을 허용한다")
	void redisFailureFallsBackToActiveSubscription() {
		MemberUuid memberUuid = new MemberUuid(UUID.randomUUID());
		CreatorUuid creatorUuid = new CreatorUuid(UUID.randomUUID());
		MembershipUuid membershipUuid = new MembershipUuid(UUID.randomUUID());
		saveMembershipPort.save(Membership.apply(
				membershipUuid,
				creatorUuid,
				"윤휘명의 여행 멤버십",
				"월간 멤버십",
				100,
				Instant.parse("2026-08-22T00:00:00Z")
		).approve(AdminUuid.from("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")));
		saveSubscriptionPort.save(MembershipSubscription.active(
				new SubscriptionUuid(UUID.randomUUID()),
				membershipUuid,
				memberUuid,
				Instant.parse("2026-08-22T00:01:00Z")
		));

		ContentAccessResult result = checkContentAccessQueryUseCase.check(
				new CheckContentAccessQuery(memberUuid, creatorUuid)
		);

		assertThat(entitlementCachePort).isInstanceOf(RedisEntitlementCacheAdapter.class);
		assertThat(result.allowed()).isTrue();
		assertThat(result.status()).isEqualTo("ACTIVE");
	}

	@TestConfiguration
	static class FailingRedisCacheConfig {

		@Bean
		@Primary
		EntitlementCachePort failingRedisEntitlementCache() {
			StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
			@SuppressWarnings("unchecked")
			ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);
			when(redisTemplate.opsForValue()).thenReturn(valueOperations);
			when(valueOperations.get(anyString())).thenThrow(new IllegalStateException("redis down"));
			Mockito.doThrow(new IllegalStateException("redis down"))
					.when(valueOperations)
					.set(anyString(), anyString(), Mockito.any());
			when(redisTemplate.delete(anyString())).thenThrow(new IllegalStateException("redis down"));
			return new RedisEntitlementCacheAdapter(redisTemplate, new MembershipCacheProperties());
		}
	}
}
