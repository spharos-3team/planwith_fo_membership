package com.planwith.planwith_fo_membership.adapter.in.web;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.planwith.planwith_fo_membership.adapter.in.web.dto.CreatorSubscribersResponse;
import com.planwith.planwith_fo_membership.adapter.in.web.dto.JoinedMembershipResponse;
import com.planwith.planwith_fo_membership.adapter.in.web.dto.MyMembershipResponse;
import com.planwith.planwith_fo_membership.adapter.in.web.dto.RevenueResponse;
import com.planwith.planwith_fo_membership.application.port.in.query.GetMyMembershipQueryUseCase;
import com.planwith.planwith_fo_membership.application.port.in.query.GetRevenueQueryUseCase;
import com.planwith.planwith_fo_membership.application.port.in.query.ListCreatorSubscribersQueryUseCase;
import com.planwith.planwith_fo_membership.application.port.in.query.ListJoinedMembershipsQueryUseCase;
import com.planwith.planwith_fo_membership.application.query.CreatorSubscribersResult;
import com.planwith.planwith_fo_membership.application.query.GetMyMembershipQuery;
import com.planwith.planwith_fo_membership.application.query.GetRevenueQuery;
import com.planwith.planwith_fo_membership.application.query.ListCreatorSubscribersQuery;
import com.planwith.planwith_fo_membership.application.query.ListJoinedMembershipsQuery;
import com.planwith.planwith_fo_membership.application.query.MyMembershipResult;
import com.planwith.planwith_fo_membership.application.query.RevenueResult;
import com.planwith.planwith_fo_membership.domain.model.vo.CreatorUuid;
import com.planwith.planwith_fo_membership.domain.model.vo.MemberUuid;

@RestController
@RequestMapping("/api/planwith-fo-membership")
public class MembershipManagementQueryController {

	private static final Logger log = LoggerFactory.getLogger(MembershipManagementQueryController.class);

	private final GetMyMembershipQueryUseCase getMyMembershipQueryUseCase;
	private final ListJoinedMembershipsQueryUseCase listJoinedMembershipsQueryUseCase;
	private final ListCreatorSubscribersQueryUseCase listCreatorSubscribersQueryUseCase;
	private final GetRevenueQueryUseCase getRevenueQueryUseCase;

	public MembershipManagementQueryController(
			GetMyMembershipQueryUseCase getMyMembershipQueryUseCase,
			ListJoinedMembershipsQueryUseCase listJoinedMembershipsQueryUseCase,
			ListCreatorSubscribersQueryUseCase listCreatorSubscribersQueryUseCase,
			GetRevenueQueryUseCase getRevenueQueryUseCase
	) {
		this.getMyMembershipQueryUseCase = getMyMembershipQueryUseCase;
		this.listJoinedMembershipsQueryUseCase = listJoinedMembershipsQueryUseCase;
		this.listCreatorSubscribersQueryUseCase = listCreatorSubscribersQueryUseCase;
		this.getRevenueQueryUseCase = getRevenueQueryUseCase;
	}

	// 본인 멤버십 조회
	@GetMapping("/memberships/me")
	public ResponseEntity<MyMembershipResponse> getMyMembership(
			@RequestHeader("X-Auth-User-Id") UUID memberUuid
	) {
		log.info("MembershipManagementQueryController : GET getMyMembership : 본인 멤버십 조회 요청 - memberUuid={}", memberUuid);
		MyMembershipResponse response = getMyMembershipQueryUseCase.get(new GetMyMembershipQuery(new CreatorUuid(memberUuid)))
				.map(MembershipManagementQueryController::toMyMembershipResponse)
				.orElseGet(() -> new MyMembershipResponse(false, null, null, null, null, null, null));
		log.info(
				"MembershipManagementQueryController : GET getMyMembership : 본인 멤버십 조회 완료 - memberUuid={}, hasMembership={}",
				memberUuid,
				response.hasMembership()
		);
		return ResponseEntity.ok(response);
	}

	// 내가 가입한 멤버십 목록 조회
	@GetMapping("/memberships/me/subscriptions")
	public ResponseEntity<List<JoinedMembershipResponse>> listJoinedMemberships(
			@RequestHeader("X-Auth-User-Id") UUID memberUuid
	) {
		log.info(
				"MembershipManagementQueryController : GET listJoinedMemberships : 가입 멤버십 목록 조회 요청 - memberUuid={}",
				memberUuid
		);
		List<JoinedMembershipResponse> response = listJoinedMembershipsQueryUseCase
				.list(new ListJoinedMembershipsQuery(new MemberUuid(memberUuid)))
				.stream()
				.map(item -> new JoinedMembershipResponse(
						item.subscriptionUuid().value(),
						item.membershipUuid().value(),
						item.creatorUuid().value(),
						item.membershipName(),
						item.monthlyPrice(),
						item.priceUnit(),
						item.status().name(),
						item.startedAt(),
						item.endedAt()
				))
				.toList();
		log.info(
				"MembershipManagementQueryController : GET listJoinedMemberships : 가입 멤버십 목록 조회 완료 - memberUuid={}, count={}",
				memberUuid,
				response.size()
		);
		return ResponseEntity.ok(response);
	}

	// Creator 가입자 목록 조회
	@GetMapping("/memberships/me/subscribers")
	public ResponseEntity<CreatorSubscribersResponse> listMySubscribers(
			@RequestHeader("X-Auth-User-Id") UUID memberUuid
	) {
		log.info(
				"MembershipManagementQueryController : GET listMySubscribers : 가입자 목록 조회 요청 - memberUuid={}",
				memberUuid
		);
		CreatorSubscribersResult result = listCreatorSubscribersQueryUseCase.list(
				new ListCreatorSubscribersQuery(new CreatorUuid(memberUuid))
		);
		CreatorSubscribersResponse response = new CreatorSubscribersResponse(
				result.subscriberCount(),
				result.subscribers().stream()
						.map(item -> new CreatorSubscribersResponse.SubscriberResponse(
								item.subscriptionUuid().value(),
								item.memberUuid().value(),
								item.status().name(),
								item.startedAt(),
								item.endedAt()
						))
						.toList()
		);
		log.info(
				"MembershipManagementQueryController : GET listMySubscribers : 가입자 목록 조회 완료 - memberUuid={}, count={}",
				memberUuid,
				response.subscriberCount()
		);
		return ResponseEntity.ok(response);
	}

	// Creator 수익 조회
	@GetMapping("/memberships/me/revenue")
	public ResponseEntity<RevenueResponse> getMyRevenue(
			@RequestHeader("X-Auth-User-Id") UUID memberUuid
	) {
		log.info("MembershipManagementQueryController : GET getMyRevenue : Creator 수익 조회 요청 - memberUuid={}", memberUuid);
		RevenueResult result = getRevenueQueryUseCase.get(new GetRevenueQuery(new CreatorUuid(memberUuid)));
		RevenueResponse response = new RevenueResponse(
				result.revenueUuid() == null ? null : result.revenueUuid().value(),
				result.totalRevenue(),
				result.availableRevenue(),
				result.reservedRevenue(),
				result.settledRevenue()
		);
		log.info(
				"MembershipManagementQueryController : GET getMyRevenue : Creator 수익 조회 완료 - memberUuid={}, totalRevenue={}, availableRevenue={}, reservedRevenue={}, settledRevenue={}",
				memberUuid,
				response.totalRevenue(),
				response.availableRevenue(),
				response.reservedRevenue(),
				response.settledRevenue()
		);
		return ResponseEntity.ok(response);
	}

	private static MyMembershipResponse toMyMembershipResponse(MyMembershipResult result) {
		return new MyMembershipResponse(
				true,
				result.membershipUuid().value(),
				result.membershipName(),
				result.monthlyPrice(),
				result.priceUnit(),
				result.status().name(),
				result.rejectReason()
		);
	}
}
