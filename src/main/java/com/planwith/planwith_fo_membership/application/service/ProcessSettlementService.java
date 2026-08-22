package com.planwith.planwith_fo_membership.application.service;

import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_membership.application.command.ApproveSettlementCommand;
import com.planwith.planwith_fo_membership.application.command.PaySettlementCommand;
import com.planwith.planwith_fo_membership.application.command.RejectSettlementCommand;
import com.planwith.planwith_fo_membership.application.event.SettlementCompletedEvent;
import com.planwith.planwith_fo_membership.application.port.in.command.ApproveSettlementUseCase;
import com.planwith.planwith_fo_membership.application.port.in.command.PaySettlementUseCase;
import com.planwith.planwith_fo_membership.application.port.in.command.RejectSettlementUseCase;
import com.planwith.planwith_fo_membership.application.port.out.LoadRevenuePort;
import com.planwith.planwith_fo_membership.application.port.out.LoadSettlementPort;
import com.planwith.planwith_fo_membership.application.port.out.MembershipEventOutboxPort;
import com.planwith.planwith_fo_membership.application.port.out.MembershipOutboxMessage;
import com.planwith.planwith_fo_membership.application.port.out.SaveRevenuePort;
import com.planwith.planwith_fo_membership.application.port.out.SaveSettlementPort;
import com.planwith.planwith_fo_membership.application.query.ProcessSettlementResult;
import com.planwith.planwith_fo_membership.domain.event.MembershipEventTypes;
import com.planwith.planwith_fo_membership.domain.exception.InvalidRevenueException;
import com.planwith.planwith_fo_membership.domain.exception.SettlementAlreadyProcessedException;
import com.planwith.planwith_fo_membership.domain.exception.SettlementNotFoundException;
import com.planwith.planwith_fo_membership.domain.model.MembershipRevenue;
import com.planwith.planwith_fo_membership.domain.model.MembershipSettlement;
import com.planwith.planwith_fo_membership.domain.model.vo.SettlementUuid;
import com.planwith.planwith_fo_membership.domain.service.AccessPolicy;

@Service
public class ProcessSettlementService implements
		ApproveSettlementUseCase,
		RejectSettlementUseCase,
		PaySettlementUseCase {

	private static final Logger log = LoggerFactory.getLogger(ProcessSettlementService.class);

	private final LoadSettlementPort loadSettlementPort;
	private final SaveSettlementPort saveSettlementPort;
	private final LoadRevenuePort loadRevenuePort;
	private final SaveRevenuePort saveRevenuePort;
	private final MembershipEventOutboxPort membershipEventOutboxPort;
	private final ObjectMapper objectMapper;

	public ProcessSettlementService(
			LoadSettlementPort loadSettlementPort,
			SaveSettlementPort saveSettlementPort,
			LoadRevenuePort loadRevenuePort,
			SaveRevenuePort saveRevenuePort,
			MembershipEventOutboxPort membershipEventOutboxPort,
			ObjectMapper objectMapper
	) {
		this.loadSettlementPort = loadSettlementPort;
		this.saveSettlementPort = saveSettlementPort;
		this.loadRevenuePort = loadRevenuePort;
		this.saveRevenuePort = saveRevenuePort;
		this.membershipEventOutboxPort = membershipEventOutboxPort;
		this.objectMapper = objectMapper;
	}

	@Override
	@Transactional
	public ProcessSettlementResult approve(ApproveSettlementCommand command) {
		AccessPolicy.requireAdmin(command.adminUuid());
		MembershipSettlement settlement = requireSettlement(command.settlementUuid());
		try {
			Instant processedAt = command.processedAt() == null ? Instant.now() : command.processedAt();
			MembershipSettlement approved = settlement.approve(command.adminUuid(), processedAt);
			saveSettlementPort.save(approved);
			MembershipRevenue revenue = requireRevenue(approved);
			log.info(
					"ProcessSettlementService : approve : 정산 승인 완료 - settlementUuid={}, adminUuid={}, status={}",
					approved.settlementUuid(),
					command.adminUuid(),
					approved.settlementStatus()
			);
			return toResult(approved, revenue);
		} catch (SettlementAlreadyProcessedException exception) {
			log.warn(
					"ProcessSettlementService : approve : 잘못된 정산 승인 요청 - settlementUuid={}, status={}",
					settlement.settlementUuid(),
					settlement.settlementStatus()
			);
			throw exception;
		}
	}

	@Override
	@Transactional
	public ProcessSettlementResult reject(RejectSettlementCommand command) {
		AccessPolicy.requireAdmin(command.adminUuid());
		MembershipSettlement settlement = requireSettlement(command.settlementUuid());
		try {
			MembershipSettlement rejected = settlement.reject(command.adminUuid(), command.rejectReason());
			MembershipRevenue released = requireRevenue(rejected).releaseReserved(rejected.settlementAmount());
			saveSettlementPort.save(rejected);
			saveRevenuePort.save(released);
			log.info(
					"ProcessSettlementService : reject : 정산 거절 완료 - settlementUuid={}, adminUuid={}, availableRevenue={}",
					rejected.settlementUuid(),
					command.adminUuid(),
					released.availableRevenue()
			);
			return toResult(rejected, released);
		} catch (SettlementAlreadyProcessedException exception) {
			log.warn(
					"ProcessSettlementService : reject : 잘못된 정산 거절 요청 - settlementUuid={}, status={}",
					settlement.settlementUuid(),
					settlement.settlementStatus()
			);
			throw exception;
		}
	}

	@Override
	@Transactional
	public ProcessSettlementResult pay(PaySettlementCommand command) {
		AccessPolicy.requireAdmin(command.adminUuid());
		MembershipSettlement settlement = requireSettlement(command.settlementUuid());
		try {
			Instant processedAt = command.processedAt() == null ? Instant.now() : command.processedAt();
			MembershipSettlement paid = settlement.pay(command.adminUuid(), processedAt);
			MembershipRevenue confirmed = requireRevenue(paid).confirmReserved(paid.settlementAmount());
			saveSettlementPort.save(paid);
			saveRevenuePort.save(confirmed);
			membershipEventOutboxPort.save(toCompletedOutbox(paid, processedAt));
			log.info(
					"ProcessSettlementService : pay : 정산 지급 완료 - settlementUuid={}, adminUuid={}, settledRevenue={}",
					paid.settlementUuid(),
					command.adminUuid(),
					confirmed.settledRevenue()
			);
			return toResult(paid, confirmed);
		} catch (SettlementAlreadyProcessedException exception) {
			log.warn(
					"ProcessSettlementService : pay : 잘못된 정산 지급 요청 - settlementUuid={}, status={}",
					settlement.settlementUuid(),
					settlement.settlementStatus()
			);
			throw exception;
		}
	}

	private MembershipSettlement requireSettlement(SettlementUuid settlementUuid) {
		return loadSettlementPort.findByUuid(settlementUuid)
				.orElseThrow(() -> new SettlementNotFoundException("정산 신청을 찾을 수 없습니다."));
	}

	private MembershipRevenue requireRevenue(MembershipSettlement settlement) {
		return loadRevenuePort.findByUuid(settlement.revenueUuid())
				.orElseThrow(() -> new InvalidRevenueException("정산 대상 수익을 찾을 수 없습니다."));
	}

	private static ProcessSettlementResult toResult(MembershipSettlement settlement, MembershipRevenue revenue) {
		return new ProcessSettlementResult(
				settlement.settlementUuid(),
				settlement.creatorUuid(),
				settlement.processedBy(),
				settlement.settlementStatus(),
				settlement.settlementAmount(),
				revenue.availableRevenue(),
				revenue.reservedRevenue(),
				revenue.settledRevenue(),
				settlement.approvedAt(),
				settlement.paidAt(),
				settlement.rejectReason()
		);
	}

	private MembershipOutboxMessage toCompletedOutbox(MembershipSettlement settlement, Instant paidAt) {
		UUID eventUuid = UUID.randomUUID();
		SettlementCompletedEvent event = new SettlementCompletedEvent(
				eventUuid.toString(),
				settlement.creatorUuid().value().toString(),
				settlement.revenueUuid().value().toString(),
				settlement.settlementUuid().value().toString(),
				settlement.settlementAmount(),
				settlement.processedBy().toString(),
				paidAt
		);
		try {
			return new MembershipOutboxMessage(
					eventUuid.toString(),
					MembershipEventTypes.AGGREGATE_SETTLEMENT,
					settlement.settlementUuid().value().toString(),
					MembershipEventTypes.SETTLEMENT_COMPLETED,
					objectMapper.writeValueAsString(event),
					paidAt
			);
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("정산 완료 이벤트 직렬화에 실패했습니다.", exception);
		}
	}
}
