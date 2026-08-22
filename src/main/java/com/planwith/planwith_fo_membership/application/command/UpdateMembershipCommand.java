package com.planwith.planwith_fo_membership.application.command;

import java.util.UUID;

public record UpdateMembershipCommand(
		UUID membershipUuid,
		String title
) {
}
