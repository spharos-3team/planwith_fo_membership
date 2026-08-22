package com.planwith.planwith_fo_membership.adapter.out.persistence.membership;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_membership.application.port.out.SaveMembershipPort;
import com.planwith.planwith_fo_membership.domain.model.Membership;
import com.planwith.planwith_fo_membership.domain.model.MembershipStatus;
import com.planwith.planwith_fo_membership.domain.model.vo.AdminUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MembershipUuid;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MembershipJpaEntityIntegrationTest {

	@Autowired
	private SaveMembershipPort saveMembershipPort;

	@Autowired
	private SpringDataMembershipRepository repository;

	@Test
	void saveMembershipStatusAsVarchar() {
		MembershipUuid membershipUuid = new MembershipUuid(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
		Membership applied = Membership.apply(
				membershipUuid,
				new CreatorUuid(UUID.fromString("22222222-2222-2222-2222-222222222222")),
				"크리에이터 멤버십",
				"월간 멤버십",
				12900,
				Instant.parse("2026-08-22T00:00:00Z")
		).approve(new AdminUuid(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")));
		saveMembershipPort.save(applied);

		MembershipJpaEntity saved = repository.findByMembershipUuid(membershipUuid.value()).orElseThrow();
		assertThat(saved.toDomain().membershipName()).isEqualTo("크리에이터 멤버십");
		assertThat(saved.toDomain().monthlyPrice()).isEqualTo(12900);
		assertThat(saved.toDomain().status()).isEqualTo(MembershipStatus.APPROVED);
		assertThat(saved.toDomain().adminUuid().toString()).isEqualTo("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
	}
}
