package com.planwith.planwith_fo_membership.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "membership.cache")
public class MembershipCacheProperties {

	private String keyPrefix = "membership:entitlement";
	private Duration ttl = Duration.ofMinutes(10);

	public String getKeyPrefix() {
		return keyPrefix;
	}

	public void setKeyPrefix(String keyPrefix) {
		this.keyPrefix = keyPrefix;
	}

	public Duration getTtl() {
		return ttl;
	}

	public void setTtl(Duration ttl) {
		this.ttl = ttl;
	}

	public String entitlementKey(String memberUuid, String creatorUuid) {
		return keyPrefix + ":" + memberUuid + ":" + creatorUuid;
	}
}
