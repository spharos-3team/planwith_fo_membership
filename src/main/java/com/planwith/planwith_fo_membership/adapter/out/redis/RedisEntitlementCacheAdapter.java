package com.planwith.planwith_fo_membership.adapter.out.redis;

import java.time.Duration;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_membership.application.port.out.EntitlementCachePort;
import com.planwith.planwith_fo_membership.application.query.ContentAccessResult;
import com.planwith.planwith_fo_membership.config.MembershipCacheProperties;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;

@Profile("!test")
@Component
public class RedisEntitlementCacheAdapter implements EntitlementCachePort {

	private static final Logger log = LoggerFactory.getLogger(RedisEntitlementCacheAdapter.class);

	private final StringRedisTemplate redisTemplate;
	private final ObjectMapper objectMapper;
	private final MembershipCacheProperties properties;

	public RedisEntitlementCacheAdapter(
			StringRedisTemplate redisTemplate,
			ObjectMapper objectMapper,
			MembershipCacheProperties properties
	) {
		this.redisTemplate = redisTemplate;
		this.objectMapper = objectMapper;
		this.properties = properties;
	}

	@Override
	public Optional<ContentAccessResult> find(MemberUuid memberUuid, CreatorUuid creatorUuid) {
		String key = properties.entitlementKey(memberUuid.toString(), creatorUuid.toString());
		try {
			String value = redisTemplate.opsForValue().get(key);
			if (value == null || value.isBlank()) {
				log.debug("RedisEntitlementCacheAdapter : find : Entitlement 캐시 MISS - key={}", key);
				return Optional.empty();
			}
			CachedEntitlement cached = objectMapper.readValue(value, CachedEntitlement.class);
			log.debug("RedisEntitlementCacheAdapter : find : Entitlement 캐시 HIT - key={}", key);
			return Optional.of(new ContentAccessResult(memberUuid, creatorUuid, cached.allowed()));
		} catch (Exception exception) {
			log.warn(
					"RedisEntitlementCacheAdapter : find : Redis 조회 실패로 Subscription 조회로 fallback - key={}",
					key
			);
			return Optional.empty();
		}
	}

	@Override
	public void save(ContentAccessResult result) {
		String key = properties.entitlementKey(result.memberUuid().toString(), result.creatorUuid().toString());
		try {
			Duration ttl = properties.getTtl() == null ? Duration.ofMinutes(10) : properties.getTtl();
			redisTemplate.opsForValue().set(
					key,
					objectMapper.writeValueAsString(new CachedEntitlement(result.allowed())),
					ttl
			);
			log.debug("RedisEntitlementCacheAdapter : save : Entitlement 캐시 저장 완료 - key={}", key);
		} catch (JsonProcessingException | RuntimeException exception) {
			log.warn("RedisEntitlementCacheAdapter : save : Redis 저장 실패 - key={}", key);
		}
	}

	@Override
	public void evict(MemberUuid memberUuid, CreatorUuid creatorUuid) {
		String key = properties.entitlementKey(memberUuid.toString(), creatorUuid.toString());
		try {
			redisTemplate.delete(key);
			log.debug("RedisEntitlementCacheAdapter : evict : Entitlement 캐시 삭제 완료 - key={}", key);
		} catch (RuntimeException exception) {
			log.warn("RedisEntitlementCacheAdapter : evict : Redis 삭제 실패 - key={}", key);
		}
	}

	private record CachedEntitlement(boolean allowed) {
	}
}
