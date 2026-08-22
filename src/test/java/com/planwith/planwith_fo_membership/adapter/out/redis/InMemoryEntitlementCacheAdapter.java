package com.planwith.planwith_fo_membership.adapter.out.redis;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_membership.application.port.out.EntitlementCachePort;
import com.planwith.planwith_fo_membership.domain.model.Entitlement;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;

@Profile("test")
@Component
public class InMemoryEntitlementCacheAdapter implements EntitlementCachePort {

	private final Map<String, Entitlement> values = new ConcurrentHashMap<>();

	@Override
	public Optional<Entitlement> find(MemberUuid memberUuid, CreatorUuid creatorUuid) {
		return Optional.ofNullable(values.get(key(memberUuid, creatorUuid)));
	}

	@Override
	public void save(Entitlement entitlement) {
		if (!entitlement.allowed()) {
			evict(entitlement.memberUuid(), entitlement.creatorUuid());
			return;
		}
		values.put(key(entitlement.memberUuid(), entitlement.creatorUuid()), entitlement);
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
