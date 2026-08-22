package com.planwith.planwith_fo_membership.adapter.out.grade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_membership.application.port.out.GradeQueryPort;
import com.planwith.planwith_fo_membership.application.query.MemberGradeResult;
import com.planwith.planwith_fo_membership.domain.exception.UnsupportedMembershipOperationException;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;

@Component
public class StubGradeQueryAdapter implements GradeQueryPort {

	private static final Logger log = LoggerFactory.getLogger(StubGradeQueryAdapter.class);

	@Override
	public MemberGradeResult getMemberGrade(MemberUuid memberUuid) {
		log.debug(
				"StubGradeQueryAdapter : getMemberGrade : Grade 서비스 조회는 후속 이슈에서 구현한다 - memberUuid={}",
				memberUuid
		);
		throw new UnsupportedMembershipOperationException("Grade 서비스 등급 조회는 후속 이슈에서 구현한다.");
	}
}
