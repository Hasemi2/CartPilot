# CartPilot 아키텍처

## 1. 목표와 경계

CartPilot은 단일 Spring Boot 애플리케이션 안에서 도메인별 패키지를 분리하는 Modular Monolith로 시작한다. 학습과 도메인 정합성에 집중하고, 독립 배포가 필요한 근거가 생기기 전에는 마이크로서비스로 분리하지 않는다.

AI는 의도 해석, 상품 탐색 조건 정리, Tool 선택, 결과 설명을 담당한다. 상품 ID, 가격, 재고, 장바구니 소유권, 주문 상태, 결제 결과의 원본은 PostgreSQL과 검증된 도메인 서비스다.

## 2. 런타임 구조

```text
Client
  |
  v
Spring MVC REST API -------------------- 인증 사용자와 명시적 주문 확인
  |
  +-- catalog application service  ----- 상품 사실·가격·판매 상태
  +-- inventory application service ---- 가용 재고·예약·해제
  +-- cart application service --------- 사용자 소유 장바구니
  +-- order application service -------- 서버 견적·상태 전이
  +-- payment application service ------ Mock PG·멱등성·대사
  |
  +-- agent orchestrator
        |
        +-- Spring AI ChatClient -------- LLM
        +-- Tool adapters --------------- 위 application service만 호출

PostgreSQL: 거래 데이터의 최종 근거
pgvector: 설명·가이드·정책 검색 보조 수단(RAG 단계)
Redis: 명확한 캐시 또는 TTL 요구가 생겼을 때만 사용
```

## 3. 패키지와 의존 방향

- `catalog`: 상품 정보, 설명, 가격 입력, 게시 상태
- `inventory`: 재고 조회, 예약, 해제, 동시성 규칙
- `cart`: 인증 사용자별 장바구니와 수량 규칙
- `order`: 서버 견적, 명시적 확인, 주문 상태 전이
- `payment`: Mock PG, 결제 시도, 멱등키, 콜백과 타임아웃
- `agent`: 프롬프트, 대화 오케스트레이션, Tool 입출력 어댑터
- `common`: 오류 응답처럼 여러 도메인이 실제로 공유하는 최소 요소

컨트롤러와 Agent Tool은 application service를 호출한다. Tool이 repository를 직접 호출하거나 도메인 엔티티를 LLM 입출력 모델로 노출하지 않는다. 도메인 간 협력이 필요하면 한 application service가 다른 도메인의 공개 유스케이스를 호출하며, 내부 repository에는 접근하지 않는다.

## 4. AI Tool Calling 경계

초기 Tool은 다음처럼 조회와 제한된 장바구니 변경에 집중한다.

- `searchProducts(query, filters)`: 게시된 상품 후보 조회
- `getProductDetails(productId)`: 현재 상품 사실과 가격 재조회
- `checkStock(productId, quantity)`: 현재 구매 가능 여부 조회
- `addCartItem(productId, quantity)`: 인증 사용자의 장바구니에 제한된 수량 추가
- `getCart()`: 인증 사용자의 장바구니 조회
- `getOrderQuote()`: 서버가 현재 가격과 재고로 계산한 견적 조회
- `getMyOrderStatus(orderId)`: 소유권을 검증한 주문 상태 조회

모든 Tool 호출에는 다음 규칙을 적용한다.

1. 사용자 ID와 권한은 인증 컨텍스트에서만 얻는다.
2. Tool 입력은 구조화된 DTO로 받고 길이, 수량, 필터 범위를 제한한다.
3. 가격·재고·소유권을 application service가 매 호출 시 검증한다.
4. 변경 Tool은 감사 가능한 결과를 남기고 재시도·중복을 고려한다.
5. 상품 설명과 RAG 문서는 명령이 아닌 신뢰할 수 없는 데이터로 취급한다.
6. LLM 응답에는 내부 엔티티 대신 허용된 응답 DTO만 전달한다.

`confirmOrder`, `approvePayment` 같은 Tool은 등록하지 않는다. 주문 확정은 별도의 인증된 REST endpoint에서 사용자의 명시적 확인을 받은 후 가격과 재고를 다시 검증해 수행한다.

## 5. 주문·결제 일관성

- 금액은 통화와 함께 `BigDecimal`로 저장하고 명시적인 반올림 정책을 둔다.
- 주문 확인 시 견적의 가격·수량·재고를 서버가 재검증한다.
- 재고 예약은 동시 요청에서 음수가 되지 않도록 DB 수준의 원자적 갱신 또는 잠금을 사용한다.
- 외부 LLM이나 PG 호출 동안 DB 트랜잭션을 열어 두지 않는다.
- 결제 시도는 멱등키와 결과를 영속화한다.
- 타임아웃은 실패로 단정하지 않고 `UNKNOWN`처럼 대사 가능한 상태로 관리한다.

## 6. 초기 운영 결정

- 로컬 PostgreSQL은 `infra/compose.yaml`로 실행한다.
- smoke test는 Docker 없이 실행되는 MVC slice로 유지한다.
- DB 엔티티가 도입되는 Commerce 단계부터 Flyway와 Testcontainers 통합 테스트를 추가한다.
- Spring AI, pgvector, Redis는 각각 Tool Calling, RAG, 명확한 캐시 요구 단계에서 추가한다.

