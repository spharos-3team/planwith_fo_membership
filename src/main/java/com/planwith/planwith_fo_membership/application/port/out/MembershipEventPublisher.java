package com.planwith.planwith_fo_membership.application.port.out;

import java.util.concurrent.CompletableFuture;

public interface MembershipEventPublisher {

	CompletableFuture<Void> publish(String topic, String key, String payload);
}
