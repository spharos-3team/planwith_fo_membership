package com.planwith.planwith_fo_membership.adapter.out.persistence.outbox;

import java.util.ArrayList;
import java.util.List;

import com.planwith.planwith_fo_membership.application.port.out.MembershipEventOutboxPort;
import com.planwith.planwith_fo_membership.application.port.out.MembershipOutboxMessage;

public class InMemoryMembershipEventOutboxPort implements MembershipEventOutboxPort {

	private final List<MembershipOutboxMessage> messages = new ArrayList<>();

	@Override
	public void save(MembershipOutboxMessage message) {
		messages.add(message);
	}

	public List<MembershipOutboxMessage> messages() {
		return List.copyOf(messages);
	}
}
