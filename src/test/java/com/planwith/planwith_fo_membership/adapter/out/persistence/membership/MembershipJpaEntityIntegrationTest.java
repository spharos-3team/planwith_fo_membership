package com.planwith.planwith_fo_membership.adapter.out.persistence.membership;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_membership.application.port.out.LoadMembershipPort;
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
	private LoadMembershipPort loadMembershipPort;

	@Autowired
	private EntityManager entityManager;

	@Test
	void saveAndLoadMembershipWithCreateAtColumn() {
		MembershipUuid membershipUuid = MembershipUuid.from("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
		CreatorUuid creatorUuid = CreatorUuid.from("22222222-2222-2222-2222-222222222222");
		Instant createAt = Instant.parse("2026-08-22T00:00:00Z");
		Membership applied = Membership.apply(
				membershipUuid,
				creatorUuid,
				"크리에이터 멤버십",
				"월간 멤버십",
				12900,
				createAt
		).approve(AdminUuid.from("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"));
		saveMembershipPort.save(applied);

		Membership loaded = loadMembershipPort.findByUuid(membershipUuid).orElseThrow();
		assertThat(loaded.membershipName()).isEqualTo("크리에이터 멤버십");
		assertThat(loaded.monthlyPrice()).isEqualTo(12900);
		assertThat(loaded.status()).isEqualTo(MembershipStatus.APPROVED);
		assertThat(loaded.adminUuid().toString()).isEqualTo("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
		assertThat(loaded.createAt()).isEqualTo(createAt);
		assertThat(loadMembershipPort.findLatestByCreator(creatorUuid))
				.get()
				.extracting(Membership::membershipUuid)
				.isEqualTo(membershipUuid);

		Object persistedCreateAt = entityManager.createNativeQuery(
						"select create_at from membership where membership_uuid = :membershipUuid"
				)
				.setParameter("membershipUuid", membershipUuid.value().toString())
				.getSingleResult();
		assertThat(persistedCreateAt).isNotNull();
	}
}
