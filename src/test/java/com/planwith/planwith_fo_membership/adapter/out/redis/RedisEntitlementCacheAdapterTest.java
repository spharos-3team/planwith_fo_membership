package com.planwith.planwith_fo_membership.adapter.out.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.planwith.planwith_fo_membership.config.MembershipCacheProperties;
import com.planwith.planwith_fo_membership.domain.model.Entitlement;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_membership.domain.service.EntitlementPolicy;

class RedisEntitlementCacheAdapterTest {

	private final MemberUuid memberUuid = MemberUuid.from("11111111-1111-1111-1111-111111111111");
	private final CreatorUuid creatorUuid = CreatorUuid.from("22222222-2222-2222-2222-222222222222");
	private final String key = "membership:entitlement:11111111-1111-1111-1111-111111111111:22222222-2222-2222-2222-222222222222";

	private StringRedisTemplate redisTemplate;
	private ValueOperations<String, String> valueOperations;
	private RedisEntitlementCacheAdapter adapter;

	@BeforeEach
	void setUp() {
		redisTemplate = Mockito.mock(StringRedisTemplate.class);
		valueOperations = Mockito.mock(ValueOperations.class);
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		MembershipCacheProperties properties = new MembershipCacheProperties();
		adapter = new RedisEntitlementCacheAdapter(redisTemplate, properties);
	}

	@Test
	void findReturnsCachedActiveWhenValueIsActive() {
		when(valueOperations.get(key)).thenReturn(EntitlementPolicy.CACHE_VALUE_ACTIVE);

		assertThat(adapter.find(memberUuid, creatorUuid).orElseThrow().allowed()).isTrue();
	}

	@Test
	void findFallsBackWhenRedisFails() {
		when(valueOperations.get(key)).thenThrow(new IllegalStateException("redis down"));

		assertThat(adapter.find(memberUuid, creatorUuid)).isEmpty();
	}

	@Test
	void saveWritesActiveStringOnly() {
		adapter.save(Entitlement.cachedActive(memberUuid, creatorUuid));

		verify(valueOperations).set(eq(key), eq("ACTIVE"), eq(Duration.ofMinutes(10)));
	}

	@Test
	void saveDeniedEvictsInsteadOfWriting() {
		adapter.save(Entitlement.denied(memberUuid, creatorUuid));

		verify(valueOperations, never()).set(eq(key), any(), any());
		verify(redisTemplate).delete(key);
	}

	@Test
	void saveFailureDoesNotPropagate() {
		doThrow(new IllegalStateException("redis down"))
				.when(valueOperations)
				.set(eq(key), eq("ACTIVE"), any(Duration.class));

		adapter.save(Entitlement.cachedActive(memberUuid, creatorUuid));
	}
}
