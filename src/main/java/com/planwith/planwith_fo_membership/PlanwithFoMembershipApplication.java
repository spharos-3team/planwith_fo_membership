package com.planwith.planwith_fo_membership;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.planwith.planwith_fo_membership.config.AuthProperties;
import com.planwith.planwith_fo_membership.config.DeployProperties;

@SpringBootApplication
@EnableConfigurationProperties({AuthProperties.class, DeployProperties.class})
public class PlanwithFoMembershipApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlanwithFoMembershipApplication.class, args);
	}

}
