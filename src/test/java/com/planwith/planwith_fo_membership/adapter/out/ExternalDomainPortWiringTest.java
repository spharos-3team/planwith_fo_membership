package com.planwith.planwith_fo_membership.adapter.out;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.planwith.planwith_fo_membership.adapter.out.follow.StubFollowQueryAdapter;
import com.planwith.planwith_fo_membership.adapter.out.grade.StubGradeQueryAdapter;
import com.planwith.planwith_fo_membership.adapter.out.token.StubTokenCommandAdapter;
import com.planwith.planwith_fo_membership.application.port.out.FollowQueryPort;
import com.planwith.planwith_fo_membership.application.port.out.GradeQueryPort;
import com.planwith.planwith_fo_membership.application.port.out.TokenCommandPort;

@SpringBootTest
@ActiveProfiles("test")
class ExternalDomainPortWiringTest {

	@Autowired
	private GradeQueryPort gradeQueryPort;

	@Autowired
	private FollowQueryPort followQueryPort;

	@Autowired
	private TokenCommandPort tokenCommandPort;

	@Test
	void applicationDependsOnPortsNotExternalDatabases() {
		assertThat(gradeQueryPort).isInstanceOf(StubGradeQueryAdapter.class);
		assertThat(followQueryPort).isInstanceOf(StubFollowQueryAdapter.class);
		assertThat(tokenCommandPort).isInstanceOf(StubTokenCommandAdapter.class);
	}
}
