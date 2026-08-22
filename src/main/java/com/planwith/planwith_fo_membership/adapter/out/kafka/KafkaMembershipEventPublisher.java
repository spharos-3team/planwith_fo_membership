package com.planwith.planwith_fo_membership.adapter.out.kafka;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_membership.application.port.out.MembershipEventPublisher;

import org.springframework.kafka.core.KafkaTemplate;

@Profile("!test")
@Component
public class KafkaMembershipEventPublisher implements MembershipEventPublisher {

	private static final Logger log = LoggerFactory.getLogger(KafkaMembershipEventPublisher.class);

	private final KafkaTemplate<String, String> kafkaTemplate;

	public KafkaMembershipEventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	@Override
	public CompletableFuture<Void> publish(String topic, String key, String payload) {
		log.info("KafkaMembershipEventPublisher : publish : 멤버십 이벤트 Kafka 발행 시작 - topic={}, key={}",
				topic, key);
		return kafkaTemplate.send(topic, key, payload)
				.thenAccept(result -> log.info(
						"KafkaMembershipEventPublisher : publish : 멤버십 이벤트 Kafka 발행 완료 - topic={}, key={}",
						topic, key
				));
	}
}
