# Release Note / RM — planwith-fo-membership

## 1. 서비스 개요

Creator가 Explorer 이상 등급으로 멤버십을 신청·승인받고, Subscriber가 Follow 검증 후 Token 차감 Saga로 가입하며, 수익 7:3 배분과 Creator 정산까지 담당하는 FO MSA 서비스이다.

| 항목 | 값 |
| --- | --- |
| 서비스명 | `planwith-fo-membership` |
| 저장소 | `planwith_fo_membership` |
| 아키텍처 | Hexagonal (adapter / application / domain) |
| 스택 | Spring Boot 4.0.7, Java 17, JPA, MySQL, Redis, Kafka |
| 포트 | `8087` (서버 localhost 디버그, 외부는 Gateway `:8000`) |
| Eureka | `planwith-fo-membership` (`lb://planwith-fo-membership`) |
| API prefix | `/api/planwith-fo-membership` |
| DB | `membership_db` / `membership_user` |
| 이미지 | `planwith/planwith-fo-membership:latest` |
| 배포 확인 | `GET /api/planwith-fo-membership/deploy-check` |

핵심 흐름:

```
Creator: Explorer 이상 → 신청(PENDING) → 관리자 승인(APPROVED)
Subscriber: Follow → 가입 자격 → Token 차감 Saga → Subscription ACTIVE → Redis Entitlement → Creator 30% 적립
Revenue: Token × 100원 → 회사 70% / Creator 30% → 정산 REQUESTED → APPROVED → PAID
```

---

## 2. 도메인 범위

### 포함

- 멤버십 신청 / 검증 / 관리자 승인·거절
- Follow 기반 가입 자격, Token 결제 시작, 가입 Saga
- Subscription ACTIVE/INACTIVE, 해지, 30일 만료 배치
- Redis Entitlement (Cache Aside, Redis 장애 시 Subscription DB fallback)
- 수익 원장(1 Token = 100원, Creator 30% / 회사 70%) 및 수익 조회
- 정산 신청 / 관리자 승인·거절·지급
- Transactional Outbox, 수신 이벤트 멱등(`processed_membership_event`)

### 상태 Enum

| 도메인 | 값 |
| --- | --- |
| Membership | `PENDING` / `APPROVED` / `REJECTED` / `INACTIVE` |
| Payment | `READY` / `SUCCESS` / `FAILED` / `CANCELED` |
| Subscription | `ACTIVE` / `INACTIVE` |
| Settlement | `REQUESTED` / `APPROVED` / `PAID` / `REJECTED` |
| Saga | `SUBSCRIBE_REQUESTED` / `PAYMENT_PENDING` / `PAYMENT_COMPLETED` / `ACTIVE` / `COMPENSATING` / `FAILED` |

### 주요 정책

- 개설 자격: 등급 레벨 ≥ 4 또는 `EXPLORER` / `ADVENTURE` / `PLANWITH`
- 가격 단위: `TOKEN`만 허용, `monthlyPrice > 0`
- 가입: 승인된 멤버십 + Follow 필수, 중복 ACTIVE 구독 불가
- READY 결제 생성 시 `paid_at = NULL`, SUCCESS 시점에만 기록
- 구독 기간: 최초 가입 후 30일(`SubscriptionPolicy.DEFAULT_TERM`), 자동 월 재결제는 미구현
- Entitlement 캐시: key `membership:entitlement:{memberUuid}:{creatorUuid}`, TTL 10분, 값 `ACTIVE`

### 테이블 (JPA `ddl-auto=update`)

`membership`, `membership_subscription`, `membership_payment`, `membership_saga`, `membership_revenue`, `membership_revenue_ledger`, `membership_settlement`, `membership_outbox`, `processed_membership_event`

### 의도적 제외

- 외부 PG / Payment Service 실결제 (Token 차감 Saga로 대체)
- 멤버십 직접 생성·수정 API (`Create` / `Update` UseCase는 stub)
- 정산 단건 조회 API (`GetSettlementQueryUseCase`는 stub)
- 자동 월 재결제 Scheduler (`next_billing_at` / `billing_cycle` 없음)

---

## 3. API 그룹

인증 헤더: 회원 `X-Member-UUID`, 관리자 `X-Admin-UUID`

| 그룹 | Method | URL | 설명 |
| --- | --- | --- | --- |
| 신청 | POST | `/memberships/applications/validate` | 등급·가격 단위 검증 |
| 신청 | POST | `/memberships/applications` | 멤버십 신청 (201, PENDING + Revenue 초기화) |
| 관리자 | POST | `/admin/memberships/{membershipUuid}/approve` | 승인 → APPROVED |
| 관리자 | POST | `/admin/memberships/{membershipUuid}/reject` | 거절 → REJECTED |
| 가입 | POST | `/memberships/subscriptions/validate` | Follow·승인 여부 검증 |
| 가입 | POST | `/memberships/subscriptions/payments` | Token 결제 시작 (201, READY + Outbox) |
| 구독 | POST | `/memberships/me/subscriptions/{subscriptionUuid}/cancel` | 해지 → INACTIVE, Entitlement 삭제 |
| 정산 | POST | `/memberships/me/settlements` | 정산 신청 (201, REQUESTED, 가용 금액 예약) |
| 정산 관리자 | POST | `/admin/settlements/{settlementUuid}/approve` | 정산 승인 |
| 정산 관리자 | POST | `/admin/settlements/{settlementUuid}/reject` | 정산 거절, 가용 수익 복구 |
| 정산 관리자 | POST | `/admin/settlements/{settlementUuid}/pay` | 지급 완료 → PAID |
| 조회 | GET | `/memberships/me` | 본인 멤버십 |
| 조회 | GET | `/memberships/me/subscriptions` | 내가 가입한 멤버십 |
| 조회 | GET | `/memberships/me/subscribers` | Creator 가입자 목록 |
| 조회 | GET | `/memberships/me/revenue` | Creator 수익 |
| Entitlement | GET | `/memberships/me/entitlement/{creatorUuid}` | 콘텐츠 접근 권한 |
| 운영 | GET | `/deploy-check` | 배포 마커 |
| 운영 | POST | `/login` | 배포 확인용 로그인 (`LOGIN_ID` / `LOGIN_PASSWORD`) |

공통 오류 코드 예: `MEMBERSHIP_GRADE_NOT_ELIGIBLE`, `FOLLOW_REQUIRED`, `MEMBERSHIP_NOT_APPROVED`, `TOKEN_INSUFFICIENT`, `SETTLEMENT_AMOUNT_EXCEEDED`

---

## 4. 외부 연동

| 대상 | 방식 | 상태 | 비고 |
| --- | --- | --- | --- |
| Grade | `GradeQueryPort` | **Stub** | 신청 시 Explorer 이상 조회. 운영 HTTP Adapter는 후속 |
| Follow (Member) | `FollowQueryPort` | **Stub** | 가입 시 팔로우 여부. 운영 HTTP Adapter는 후속 |
| Token | Outbox → Kafka | **계약 구현** | `planwith.token.deduction-requested` 발행, `succeeded` / `failed` 수신 |
| Token / Payment Port | `TokenCommandPort`, `PaymentRequestPort` | Stub | 동기 호출은 사용하지 않음. 가입은 Kafka Saga |
| Payment 이벤트 | Kafka 수신 | **멱등 마커만** | `PaymentCompleted`는 구독 활성화를 하지 않음. 활성화는 TokenDeductionSucceeded |
| Redis | Entitlement Cache | **구현** | 장애 시 예외를 삼키고 Subscription DB fallback |
| Kafka Outbox Relay | `membership_outbox` | **구현, 기본 OFF** | `MEMBERSHIP_OUTBOX_ENABLED=true` 필요 |
| Eureka / Gateway | Discovery + Path 라우팅 | 구성됨 | Gateway snippet 수동 반영 |

### Kafka Topic

**수신**

- `planwith.token.deduction-succeeded` → 구독 ACTIVE, 수익 배분, Entitlement, `MembershipSubscribed`
- `planwith.token.deduction-failed` → Payment FAILED, Subscription 미생성
- `planwith.payment.completed` / `failed` / `refunded` → 멱등 기록 (구독 상태 변경 없음)

**발행 (Outbox → Relay)**

- `planwith.token.deduction-requested`
- `planwith.membership.subscribed` / `canceled` / `expired`
- `planwith.membership.settlement-requested` / `settlement-completed`

Consumer group: `membership-service`  
기본값: `MEMBERSHIP_KAFKA_CONSUMER_ENABLED=false`

---

## 5. 비기능 / 품질

- Hexagonal + 생성자 주입, Entity를 API 응답으로 직접 반환하지 않음
- 전역 예외 처리 (`GlobalExceptionHandler` + `ApiErrorResponse`)
- Bean Validation, 관리자/본인 권한 가드 (`AccessPolicy`)
- Outbox는 비즈니스 TX와 동일 트랜잭션 저장, 중복 `eventUuid` skip
- 수신 이벤트는 `eventUuid` / `paymentUuid+eventType` 기준 멱등
- Redis 장애가 API 실패로 전파되지 않음 (DB source of truth)
- 로그 포맷: `[클래스] : [메서드] : [한글 역할]`, 토큰·비밀번호 미출력
- 테스트: H2 (`MODE=MySQL`), Grade/Follow in-memory stub, Kafka consumer 직접 호출
- 통합 테스트: 신청→승인→가입→토큰→수익→정산 시나리오 22개 (`#21`)
- `./gradlew clean test`, `./gradlew build` 통과

---

## 6. 배포 설정 요약

| 항목 | 값 |
| --- | --- |
| Windows 배포 | `.github/workflows/deploy.yml` — self-hosted `planwith-server`, `develop` push |
| AWS 배포 | `.github/workflows/deploy-aws.yml` — ECR `planwith-fo-membership` → EC2 compose |
| Compose | `planwith-fo-membership`, `127.0.0.1:8087:8087`, `depends_on: discovery, mysql` |
| Gateway | `Path=/api/planwith-fo-membership/**` → `lb://planwith-fo-membership` |
| OpenAPI servers | `GATEWAY_PUBLIC_URL=/` (Swagger가 Docker hostname을 쓰지 않음) |
| Docker Swagger UI | `SPRINGDOC_SWAGGER_UI_ENABLED=false` (UI는 Gateway `:8000`) |
| JPA | `JPA_DDL_AUTO=update` (별도 Flyway 없음) |
| Health | `/actuator/health`, Kafka/Redis health 비활성 |
| 로컬 `.env.example` | MySQL `127.0.0.1:3307`, Eureka/Kafka/Outbox 기본 비활성 |

운영에서 실제 가입·이벤트 흐름을 켜려면:

```
MEMBERSHIP_KAFKA_CONSUMER_ENABLED=true
MEMBERSHIP_OUTBOX_ENABLED=true
MEMBERSHIP_EXPIRE_SCHEDULER_ENABLED=true   # 30일 만료 배치
```

---

## 7. 운영 주의사항

1. **Grade / Follow Adapter가 Stub이다.** 현재 운영 JVM에서 신청·가입 API를 호출하면 `501 NOT_IMPLEMENTED`가 난다. Grade HTTP 조회, Member Follow 조회 Adapter를 붙이기 전에는 E2E 가입이 불가하다.
2. **Kafka Consumer / Outbox Relay / 만료 Scheduler 기본값은 false**이다. 플래그를 켜지 않으면 Token 차감 응답을 못 받고, Outbox 이벤트도 Kafka로 나가지 않는다.
3. **Token 부족/성공은 Token 서비스의 Kafka 응답에 의존한다.** Membership은 동기 차감 호출을 하지 않는다.
4. **PaymentCompleted로 구독을 활성화하지 않는다.** 구독 확정은 `TokenDeductionSucceeded`만 수행한다.
5. **다른 PC는 `:8087`을 직접 호출하지 않는다.** `브라우저 → Gateway :8000 → Eureka → membership:8087`
6. **자동 월 재결제는 없다.** 이번 범위는 최초 가입 Token 차감 + 해지/30일 만료까지다.
7. **외부 PG는 범위 밖**이다. 실결제는 Payment Service 책임이다.
8. 정산 가능 금액 초과, 중복 ACTIVE 구독, 미승인 멤버십 가입은 409/403으로 거절된다.
9. JPA `ddl-auto=update`이므로 스키마 변경 시 운영 DB 사전 확인이 필요하다.

---

## 8. 개발 완료 범위 (단계 요약)

GitHub Issue `#1` ~ `#21` 전부 CLOSED.

| 단계 | 이슈 | 내용 |
| --- | --- | --- |
| 1 | #1 | 프로젝트 기본 셋팅, 배포/OpenAPI |
| 1 | #2–#3 | Domain / Persistence |
| 2 | #4–#5 | 외부 Port, Grade 기반 개설 자격 |
| 3 | #6–#8 | 신청, 관리자 승인/거절, 관리 조회 |
| 4 | #9 | Follow 기반 가입 자격 |
| 5 | #10–#11 | Token 결제 시작, 가입 Saga |
| 6 | #13–#15 | Entitlement, 구독 관리, 해지/만료 |
| 7 | #12, #16 | 수익 7:3 배분, 수익 조회 |
| 8 | #17–#18 | 정산 신청, 관리자 처리 |
| 9 | #19–#20 | Outbox/EDA, 예외·권한·Validation |
| 10 | #21 | 통합 테스트 (시나리오 1~22) |

---

## 9. 검증 상태

| 구분 | 상태 |
| --- | --- |
| 단위 테스트 (Domain / Policy / Service mock) | 통과 |
| Persistence / Outbox / Saga 통합 테스트 | 통과 |
| API MockMvc 통합 테스트 | 통과 |
| 이벤트 스토밍 22 시나리오 (`MembershipEventStormingIntegrationTest`) | 통과 |
| Redis 장애 DB fallback 통합 테스트 | 통과 |
| deploy-check / OpenAPI servers 회귀 | 통과 |
| `./gradlew clean test` | 통과 |
| `./gradlew build` | 통과 |
| Grade/Follow 실서비스 E2E | **미검증** (Port Stub) |
| Kafka 실브로커 E2E | **미검증** (테스트는 consumer 직접 호출 + H2) |
| 자동 월 재결제 | **미구현** |

---

**RM 결론:** planwith-fo-membership의 계획된 도메인(신청·승인·Token 가입 Saga·Entitlement·수익 7:3·정산·Outbox·통합 테스트)은 `#1`~`#21` 기준으로 개발 완료다. 운영 E2E를 열려면 Grade/Follow 실 Adapter 연결, `MEMBERSHIP_KAFKA_CONSUMER_ENABLED` / `MEMBERSHIP_OUTBOX_ENABLED` 활성화, Token 서비스와의 Kafka 계약 확인이 선행되어야 한다. PG 실결제와 자동 월 재결제는 이번 릴리즈 범위가 아니다.
