package com.planwith.planwith_fo_membership.application.port.out;

import com.planwith.planwith_fo_membership.application.query.MemberGradeResult;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;

public interface GradeQueryPort {

	MemberGradeResult getMemberGrade(MemberUuid memberUuid);
}
