package com.planwith.planwith_fo_membership.application.service;

import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_membership.application.command.RequestSettlementCommand;
import com.planwith.planwith_fo_membership.application.event.SettlementRequestedEvent;
import com.planwith.planwith_fo_membership.application.port.in.command.RequestSettlementUseCase;
import com.planwith.planwith_fo_membership.application.port.out.LoadRevenuePort;
import com.planwith.planwith_fo_membership.application.port.out.MembershipEventOutboxPort;
import com.planwith.planwith_fo_membership.application.port.out.MembershipOutboxMessage;
import com.planwith.planwith_fo_membership.application.port.out.SaveRevenuePort;
import com.planwith.planwith_fo_membership.application.port.out.SaveSettlementPort;
import com.planwith.planwith_fo_membership.application.query.RequestSettlementResult;
import com.planwith.planwith_fo_membership.domain.event.MembershipEventTypes;
import com.planwith.planwith_fo_membership.domain.exception.InvalidRevenueException;
import com.planwith.planwith_fo_membership.domain.exception.InvalidSettlementStateException;
import com.planwith.planwith_fo_membership.domain.model.MembershipRevenue;
import com.planwith.planwith_fo_membership.domain.model.MembershipSettlement;
import com.planwith.planwith_fo_membership.domain.model.vo.SettlementUuid;
import com.planwith.planwith_fo_membership.domain.service.SettlementPolicy;

@Service
public class RequestSettlementService implements RequestSettlementUseCase {

	private static final Logger log = LoggerFactory.getLogger(RequestSettlementService.class);

	private final LoadRevenuePort loadRevenuePort;
	private final SaveRevenuePort saveRevenuePort;
	private final SaveSettlementPort saveSettlementPort;
	private final MembershipEventOutboxPort membershipEventOutboxPort;
	private final ObjectMapper objectMapper;

	public RequestSettlementService(
			LoadRevenuePort loadRevenuePort,
			SaveRevenuePort saveRevenuePort,
			SaveSettlementPort saveSettlementPort,
			MembershipEventOutboxPort membershipEventOutboxPort,
			ObjectMapper objectMapper
	) {
		this.loadRevenuePort = loadRevenuePort;
		this.saveRevenuePort = saveRevenuePort;
		this.saveSettlementPort = saveSettlementPort;
		this.membershipEventOutboxPort = membershipEventOutboxPort;
		this.objectMapper = objectMapper;
	}

	@Override
	@Transactional
	public RequestSettlementResult request(RequestSettlementCommand command) {
		if (command.settlementAmount() <= 0) {
			throw new InvalidSettlementStateException("정산 금액은 0보다 커야 합니다.");
		}
		MembershipRevenue revenue = loadRevenuePort.findByCreator(command.creatorUuid())
				.orElseThrow(() -> new InvalidRevenueException("정산 가능한 수익이 없습니다."));
		if (!SettlementPolicy.canRequest(command.settlementAmount(), revenue.availableRevenue())) {
			throw new InvalidRevenueException("정산 가능 금액을 초과할 수 없습니다.");
		}
		Instant requestedAt = command.requestedAt() == null ? Instant.now() : command.requestedAt();
		MembershipRevenue reserved = revenue.reserve(command.settlementAmount());
		saveRevenuePort.save(reserved);
		MembershipSettlement settlement = MembershipSettlement.request(
				new SettlementUuid(UUID.randomUUID()),
				command.creatorUuid(),
				reserved.revenueUuid(),
				command.settlementAmount(),
				requestedAt
		);
		saveSettlementPort.save(settlement);
		membershipEventOutboxPort.save(toRequestedOutbox(settlement, requestedAt));
		log.info(
				"RequestSettlementService : request : 정산 신청 완료 - settlementUuid={}, creatorUuid={}, amount={}, availableRevenue={}, reservedRevenue={}",
				settlement.settlementUuid(),
				settlement.creatorUuid(),
				settlement.settlementAmount(),
				reserved.availableRevenue(),
				reserved.reservedRevenue()
		);
		return new RequestSettlementResult(
				settlement.settlementUuid(),
				settlement.settlementStatus(),
				settlement.settlementAmount(),
				reserved.availableRevenue(),
				reserved.reservedRevenue(),
				settlement.requestedAt()
		);
	}

	private MembershipOutboxMessage toRequestedOutbox(MembershipSettlement settlement, Instant requestedAt) {
		UUID eventUuid = UUID.randomUUID();
		SettlementRequestedEvent event = new SettlementRequestedEvent(
				eventUuid.toString(),
				settlement.creatorUuid().value().toString(),
				settlement.revenueUuid().value().toString(),
				settlement.settlementUuid().value().toString(),
				settlement.settlementAmount(),
				requestedAt
		);
		try {
			return new MembershipOutboxMessage(
					eventUuid.toString(),
					MembershipEventTypes.AGGREGATE_SETTLEMENT,
					settlement.settlementUuid().value().toString(),
					MembershipEventTypes.SETTLEMENT_REQUESTED,
					objectMapper.writeValueAsString(event),
					requestedAt
			);
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("정산 신청 이벤트 직렬화에 실패했습니다.", exception);
		}
	}
}
