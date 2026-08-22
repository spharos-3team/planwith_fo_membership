package com.planwith.planwith_fo_membership;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.planwith.planwith_fo_membership.config.AuthProperties;
import com.planwith.planwith_fo_membership.config.DeployProperties;
import com.planwith.planwith_fo_membership.config.MembershipCacheProperties;
import com.planwith.planwith_fo_membership.config.MembershipKafkaProperties;
import com.planwith.planwith_fo_membership.config.MembershipOutboxProperties;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({
		AuthProperties.class,
		DeployProperties.class,
		MembershipKafkaProperties.class,
		MembershipOutboxProperties.class,
		MembershipCacheProperties.class
})
public class PlanwithFoMembershipApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlanwithFoMembershipApplication.class, args);
	}
}
