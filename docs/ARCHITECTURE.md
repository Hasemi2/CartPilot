# CartPilot 아키텍처

## 1. 시스템 목표

CartPilot은 사용자의 자연어 요청을 이해한 LLM이 제한된 Commerce Tool을 선택하고, 서버가 실제 상품·재고·장바구니·주문 데이터를 검증해 처리하는 AI 커머스 애플리케이션이다.

LLM 자체를 학습하거나 호스팅하지 않는다. 외부 LLM은 의도 해석, Tool 선택, 결과 설명만 담당한다. 상품 ID, 가격, 재고, 소유권, 주문 상태, 결제 결과의 최종 권한은 도메인 서비스와 관계형 데이터베이스에 있다.

## 2. 핵심 원칙

1. 단일 Spring Boot 애플리케이션을 도메인별 패키지로 분리한 Modular Monolith로 시작한다.
2. Agent Tool은 repository가 아니라 검증된 application service만 호출한다.
3. 로컬 DB가 H2여도 도메인 규칙과 Tool 인터페이스는 바뀌지 않는다.
4. H2 테스트는 개발 편의용이며 PostgreSQL의 잠금·동시성·SQL 동작을 증명하지 않는다.
5. LLM과 RAG 결과는 신뢰하지 않고 거래 데이터는 항상 live domain API에서 재조회한다.
6. 주문 확정과 결제 승인은 LLM Tool로 제공하지 않는다.
7. 외부 LLM·PG 호출 중에는 DB 트랜잭션을 열어 두지 않는다.

## 3. 논리 구조

```text
Client
  |
  v
Spring MVC REST API ---------------- 인증·인가 / 명시적 주문 확인
  |
  +-- catalog application service --- 상품·가격·판매 상태
  +-- inventory application service - 가용 재고·예약·해제
  +-- cart application service ------ 사용자 소유 장바구니
  +-- order application service ----- 서버 견적·주문 상태
  +-- payment application service --- Mock PG·멱등성·대사
  |
  +-- agent orchestrator
        |
        +-- ChatModel port
        |     +-- StubChatModel ------- 로컬 기본값/API 키 불필요
        |     +-- Gemini Flash -------- local-ai 자연어·Tool 선택
        |
        +-- Tool adapters ------------ application service만 호출
        |
        +-- VectorStore port
              +-- SimpleVectorStore --- 로컬 학습·데모
              +-- PgVectorStore ------- PostgreSQL 검증 환경

Persistence
  +-- local-lite: H2 file database
  +-- ci-postgres: PostgreSQL
```

## 4. 실행 프로필

| 프로필 | 관계형 DB | ChatModel | VectorStore | 목적 |
|---|---|---|---|---|
| `local-lite` | H2 file | Stub | 사용 안 함 | 기본 개발·테스트 |
| `local-ai` | H2 file | Gemini 3.5 Flash Lite | 사용 안 함 | 실제 LLM Tool Calling 데모 |
| `ci-postgres` | PostgreSQL | Stub | 필요 시 PgVectorStore | DB 정합성과 벡터 검색 검증 |

`local-lite`는 Docker, PostgreSQL 설치, LLM API 키 없이 실행되어야 한다. `local-ai`는 명시적으로 프로필을 선택하고 `GOOGLE_API_KEY` 환경변수를 제공할 때만 Gemini API를 호출한다. 두 프로필은 같은 `ChatClient`와 Commerce Tool을 사용하고 `ChatModel`만 교체한다.

## 5. 도메인과 의존 방향

- `catalog`: 상품 정보, 설명, 가격 입력, 게시 상태
- `inventory`: 재고 조회, 예약, 해제, 동시성 규칙
- `cart`: 인증 사용자별 장바구니와 수량 규칙
- `order`: 서버 견적, 명시적 확인, 주문 상태 전이
- `payment`: Mock PG, 결제 시도, 멱등키, 콜백과 타임아웃
- `agent`: 프롬프트, 대화 오케스트레이션, Tool과 AI adapter
- `common`: 오류 응답처럼 여러 도메인이 실제로 공유하는 최소 요소

허용하는 의존 방향은 다음과 같다.

```text
Controller / Agent Tool
          |
          v
Application Service
          |
          v
Domain Model / Repository Port
          |
          v
JPA Adapter / External Adapter
```

컨트롤러와 Tool은 JPA 엔티티를 외부에 노출하지 않는다. 도메인 간 협력은 공개 application use case를 통해 수행하며 다른 도메인의 repository를 직접 호출하지 않는다.

## 6. 데이터베이스 전략

### 로컬 개발

H2 file mode를 사용해 별도 서버 없이 데이터를 유지한다. PostgreSQL 호환 모드는 단순 SQL 차이를 줄이는 보조 수단으로만 사용한다.

Flyway 마이그레이션은 가능한 범위에서 공통 SQL로 작성한다. PostgreSQL 전용 잠금, 인덱스, pgvector 확장은 별도 migration location 또는 프로필로 분리한다.

### PostgreSQL 검증

GitHub Actions의 Linux runner에서 PostgreSQL service container를 실행해 다음 동작을 검증한다.

- Flyway migration 적용
- JPA mapping과 실제 SQL
- 유니크·외래키·체크 제약조건
- 재고 차감의 원자성과 동시성
- 주문 상태 전이와 결제 멱등성

민감정보는 원격 CI에 올리지 않고 테스트용 가상 데이터만 사용한다.

## 7. AI Tool Calling

초기 Tool은 조회와 제한된 장바구니 변경에 집중한다.

- `searchProducts(query, filters)`
- `getProductDetails(productId)`
- `checkStock(productId, quantity)`
- `addCartItem(productId, quantity)`
- `getCart()`
- `getOrderQuote()`
- `getMyOrderStatus(orderId)`

처리 흐름은 다음과 같다.

```text
사용자 자연어 요청
  -> ChatClient가 Tool 정의와 함께 LLM 호출
  -> LLM이 Tool 이름과 구조화된 인자를 선택
  -> Tool adapter가 application service 호출
  -> 서버가 권한·가격·재고·수량을 검증
  -> Tool 결과를 LLM에 전달
  -> LLM이 실제 결과를 자연어로 설명
```

`local-ai`의 외부 호출은 요청당 15초, Spring AI 최대 2회로 제한한다. 모델 과부하 또는 시간 초과가 계속되면 Tool 실행을 이어가지 않고 `503 AI_MODEL_UNAVAILABLE`로 종료한다.

모든 Tool에 다음 규칙을 적용한다.

1. 사용자 ID와 권한은 Tool 인자가 아니라 인증 컨텍스트에서 얻는다.
2. 문자열 길이, 수량, 필터 범위를 제한한다.
3. 가격·재고·소유권을 application service가 매 호출 시 검증한다.
4. 변경 작업은 감사 가능하게 기록하고 재시도·중복을 고려한다.
5. 상품 설명과 RAG 문서에 포함된 명령문을 실행 지시로 취급하지 않는다.
6. `confirmOrder`, `approvePayment` Tool은 등록하지 않는다.

로컬 기본 테스트에서는 Stub ChatModel이 정해진 Tool 요청을 반환하도록 해 Tool dispatch와 검증 로직을 재현한다. `local-ai`에서는 Gemini 3.5 Flash Lite가 같은 Tool 정의를 보고 자연어 요청에 맞는 Tool 이름과 인자를 선택한다. 실제 API를 호출하는 테스트는 기본 테스트에서 분리한다.

## 8. RAG 구조

RAG는 거래 기능이 아니라 상품 설명·가이드·정책 문서를 의미 기반으로 찾는 보조 기능이다.

```text
색인: 문서 -> chunk -> embedding -> VectorStore
질의: 사용자 질문 -> embedding -> 유사 문서 검색 -> LLM 참고 문맥
```

- `SimpleVectorStore`: 작은 샘플 데이터로 학습·데모한다. 영속 저장소로 사용하지 않는다.
- `PgVectorStore`: PostgreSQL 검증 환경에서 영속성, 필터, 검색 품질을 확인한다.
- 현재 가격, 재고, 주문 상태는 VectorStore에서 답하지 않고 반드시 domain API로 재조회한다.

## 9. 주문·결제 불변식

- 금액은 통화와 함께 `BigDecimal`로 저장하고 명시적인 반올림 정책을 둔다.
- 주문 확인 시 견적의 가격·수량·재고를 서버가 다시 검증한다.
- 재고는 동시 요청에서도 음수가 되지 않아야 한다.
- 결제 시도는 멱등키와 결과를 영속화한다.
- 타임아웃은 실패로 단정하지 않고 `UNKNOWN`처럼 대사 가능한 상태로 관리한다.
- H2 테스트만으로 위 불변식의 DB 동시성 검증을 완료했다고 판단하지 않는다.

## 10. 테스트 계층

1. 순수 단위 테스트: 도메인 규칙, 금액 계산, 상태 전이
2. `local-lite` 통합 테스트: H2 기반 repository와 REST/Tool 흐름
3. Stub ChatModel 테스트: Tool 선택 결과, 입력 검증, 사용자 컨텍스트
4. `ci-postgres` 통합 테스트: PostgreSQL 제약조건·잠금·동시성
5. 실제 LLM smoke test: 허용된 환경에서 최소 고정 질문만 실행
6. RAG 평가: 키워드 검색과 벡터 검색의 관련성·근거 비교

## 11. 실행 환경 결정

- Docker Compose 파일은 선택적 PostgreSQL 실행 도구로 유지하며 기본 실행 경로로 간주하지 않는다.
- Testcontainers는 컨테이너 런타임이 있는 CI에서만 선택적으로 사용한다.
- Redis는 구체적인 캐시·TTL 요구가 확인되기 전에는 추가하지 않는다.
- API 키와 비밀값은 환경변수 또는 CI secret으로만 주입하고 저장소에 커밋하지 않는다.
