package com.planwith.planwith_fo_membership.adapter.out.redis;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_membership.application.port.out.EntitlementCachePort;
import com.planwith.planwith_fo_membership.application.query.ContentAccessResult;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;

@Profile("test")
@Component
public class InMemoryEntitlementCacheAdapter implements EntitlementCachePort {

	private final Map<String, ContentAccessResult> values = new ConcurrentHashMap<>();

	@Override
	public Optional<ContentAccessResult> find(MemberUuid memberUuid, CreatorUuid creatorUuid) {
		return Optional.ofNullable(values.get(key(memberUuid, creatorUuid)));
	}

	@Override
	public void save(ContentAccessResult result) {
		values.put(key(result.memberUuid(), result.creatorUuid()), result);
	}

	@Override
	public void evict(MemberUuid memberUuid, CreatorUuid creatorUuid) {
		values.remove(key(memberUuid, creatorUuid));
	}

	public void clear() {
		values.clear();
	}

	private static String key(MemberUuid memberUuid, CreatorUuid creatorUuid) {
		return memberUuid + ":" + creatorUuid;
	}
}
