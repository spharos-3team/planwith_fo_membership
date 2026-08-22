package com.planwith.planwith_fo_membership.adapter.out.kafka;

import java.util.concurrent.CompletableFuture;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_membership.application.port.out.MembershipEventPublisher;

@Profile("test")
@Component
public class NoOpMembershipEventPublisher implements MembershipEventPublisher {

	@Override
	public CompletableFuture<Void> publish(String topic, String key, String payload) {
		return CompletableFuture.completedFuture(null);
	}
}
