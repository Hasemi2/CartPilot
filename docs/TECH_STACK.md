# CartPilot — 기술 스택 및 도입 계획

## 1. 프로젝트 목표
자연어 요청으로 상품 검색·추천·장바구니 구성을 지원하는 **AI 커머스 주문 에이전트**. 가격·재고·주문·결제는 서버의 검증된 도메인 로직으로 처리한다.

**MVP 흐름**: 사용자 자연어 요청 → LLM의 Tool Calling → 실제 상품 검색/재고 확인 → 장바구니 → 서버 주문 견적 → 사용자 명시적 승인 → Mock 결제 → 주문 상태 확인.

> 원칙: AI는 *의도 해석 및 도구 선택*을 맡고, 백엔드는 *권한·정합성·트랜잭션·결제*를 책임진다.

## 2. 핵심 기술 스택

| 영역 | 기술 | 도입 시점 | 선택 이유 |
|---|---|---|---|
| 언어 | Java 21 | 초기 | LTS, 기존 Java/Spring 역량 활용 |
| Backend | Spring Boot 4.1.x, Spring MVC | 초기 | 안정적인 REST API와 동기적 비즈니스 로직 |
| Build | Gradle (Groovy DSL), Gradle Wrapper | 초기 | 재현 가능한 빌드 |
| ORM | Spring Data JPA / Hibernate | 초기 | 주문·상품 도메인 영속성 모델링 |
| DB | PostgreSQL | 초기 | 관계형 트랜잭션과 정합성 |
| DB 마이그레이션 | Flyway | 도메인 도입 시 | 스키마 버전 관리 |
| AI | Spring AI 2.0.x | Tool Calling 단계 | LLM ChatClient, Tool Calling, 평가, RAG 지원 |
| LLM | OpenAI API (환경변수 기반) | Tool Calling 단계 | 실제 자연어 요청 및 도구 선택 검증 |
| 벡터 검색 | pgvector + Spring AI PgVectorStore | RAG 단계 | PostgreSQL 기반 의미 검색 |
| 캐시 | Redis | 필요 시 | 캐시, TTL, 일부 요청 중복 방어; 결제 정합성의 단독 근거로 사용하지 않음 |
| 결제 | 자체 Mock PG | 주문/결제 단계 | 비용·보안 없이 승인/실패/타임아웃 시뮬레이션 |
| 테스트 | JUnit 5, Spring Boot Test, Testcontainers | 초기~ | DB 통합 테스트, 동시성·멱등성 검증 |
| 부하 테스트 | k6 | 안정성 검증 단계 | 처리량, 지연, 오류율 검증 |
| 개발 환경 | Docker Compose | 초기 | PostgreSQL 등 로컬 인프라 실행 |
| 관측성 | Spring Actuator, Micrometer | 안정성 검증 단계 | 요청/LLM 응답 시간, 호출 결과, 비용 관찰 |
| Frontend | React + TypeScript (선택) | MVP 후반 | 대화와 장바구니 결과 확인용 최소 UI |
| 확장 학습 | MCP Server | 선택적 확장 | 상품·주문 조회 기능을 외부 에이전트에 도구로 제공 |

### 버전 정책 (2026-11-12 검증)
- **Spring AI 2.0.x**는 **Spring Boot 4.0.x/4.1.x**를 지원한다. 초기화 버전은 최신 안정 패치인 **Spring Boot 4.1.2**이며, Tool Calling 단계에서 **Spring AI 2.0.2**를 추가한다.
- Spring AI 2.1 계열은 아직 정식 안정 버전이 아니므로 이번 프로젝트 기본 선택에서 제외한다.
- 로컬 PostgreSQL은 현재 지원되는 최신 안정 패치인 **18.6**을 사용한다. RAG 단계에서 동일 PostgreSQL 계열을 지원하는 pgvector 이미지를 검증해 전환한다.
- `build.gradle`의 Spring AI BOM과 스타터 아티팩트 이름은 선택한 버전의 공식 문서를 기준으로 적용한다.
- LLM 모델 이름은 환경설정으로 분리하고, 지원 여부와 사용요금을 호출 전에 확인한다.

## 3. 아키텍처

```text
Client (향후 React)
   │
   ▼
Spring MVC REST API  ─────────── 인증·인가 / 사용자 승인
   │
   ├── catalog ───── 상품 및 가격 데이터 ─────┐
   ├── inventory ─── 재고·예약 ──────────────┤
   ├── cart ──────── 장바구니 ──────────────┤── PostgreSQL
   ├── order ─────── 견적·주문 상태 ────────┤
   ├── payment ───── Mock PG·멱등성 ───────┘
   └── agent ─────── Spring AI ChatClient
           │          │
           │          ├── LLM API
           │          └── 명시적 Tool Calling → 검증된 application services
           └── (RAG 단계) PgVectorStore → PostgreSQL/pgvector

Redis: 명확한 캐시/TTL 요구사항이 생길 때 도입
```

**형태**: MSA가 아니라 도메인별 패키지를 가진 **Modular Monolith**. 복잡한 멀티모듈 Gradle 설정은 필요한 경우에만 도입.

## 4. 도메인 분리

- `catalog`: 상품 목록·상세·가격·설명·판매상태
- `inventory`: 재고 조회·예약·해제·동시성 제어
- `cart`: 사용자별 항목 및 수량 관리
- `order`: 서버 견적, 사용자 확인, 주문 상태 전이
- `payment`: Mock PG, 멱등키, 승인/취소/타임아웃, 결과 대사
- `agent`: LLM 프롬프트, 도구 인터페이스, 호출·응답 검증
- `common`: 최소한의 공통 관심사

## 5. AI 기능의 단계별 확장

### 1단계 — Tool Calling (필수)
- `searchProducts(keyword, filters)`
- `getProductDetails(productId)`
- `checkStock(productId)`
- `addCartItem(productId, quantity)`
- `getCart()`
- `getOrderQuote()`

사용자 식별은 서버 인증 컨텍스트를 사용한다. LLM에게 사용자 ID나 가격을 결정하도록 맡기지 않는다. AI에는 **직접 결제 승인 도구를 제공하지 않는다**.

### 2단계 — RAG (권장)
- 상품 상세 설명·사용 가이드·배송/교환 정책 문서 임베딩
- pgvector 유사도 검색과 키워드 검색의 차이 실험
- 문서 출처와 상품 ID 매핑 및 결과 검증
- **현재 가격·재고는 벡터 검색 결과가 아닌 도메인 API 재조회**

### 3단계 — MCP / 평가 (선택)
- 읽기 전용 상품 검색 Tool을 MCP 서버로 공개
- 고정 질문 세트로 제품 추천의 관련성, 도구 정확도, 환각률 측정
- API 지연, 모델 토큰, 호출 비용 기록

## 6. 결제/주문 안정성 검증 목표

1. 주문 초안의 가격을 서버에서 재계산한다.
2. 재고 10개 / 동시 주문 100개 환경에서 초과 판매가 없어야 한다.
3. 동일 결제 멱등키의 재요청으로 중복 승인이 발생하지 않아야 한다.
4. PG 승인 후 응답 타임아웃을 `UNKNOWN` 등 재조회 가능한 상태로 관리한다.
5. 중복 콜백·실패·취소에서 주문/재고 상태가 일관되어야 한다.
6. 다른 사용자의 장바구니·주문을 Agent Tool이 조회할 수 없어야 한다.

## 7. 개발 순서

| 단계 | 구현 범위 | 완료 기준 |
|---|---|---|
| 0. Init | Gradle, Spring Boot, Postgres, smoke test, 문서 | 로컬 빌드·테스트 성공 |
| 1. Commerce | 상품 조회, 재고 조회, 장바구니 | API + DB 테스트 성공 |
| 2. AI Tool Calling | 자연어 상품 검색, 장바구니 도구 호출 | AI 응답이 실제 상품 ID와 연결됨 |
| 3. Order/Payment | 견적, 명시적 주문 확인, Mock PG | 중복 결제/타임아웃 테스트 |
| 4. RAG | pgvector, 검색 평가 | 응답 근거·정확도 비교 |
| 5. Hardening | 동시성·인증·모니터링·부하 테스트 | 재현 가능한 테스트 결과 |
| 6. Optional | React UI, MCP, 배포 | 최소 데모 완성 |

## 8. 초기화 시 주의점

- **최초 init에 AI API Key를 요구하지 않는다.** 기본 빌드가 인증정보 없이 통과해야 한다.
- PostgreSQL은 초기부터 쓰되, pgvector/Redis는 관련 기능 구현 시 추가해도 충분하다.
- 최초에는 실행 가능한 단일 애플리케이션과 smoke test만 만든다. 빈 도메인 클래스 수십 개를 생성하지 않는다.
- `.env`와 API Key는 절대 커밋하지 않는다. `.env.example`만 포함한다.
- IntelliJ 또는 Codex가 로컬에서 `./gradlew test`를 실행해 검증한다.

## 9. 공식 참고자료

- Spring AI Getting Started: https://docs.spring.io/spring-ai/reference/getting-started.html
- Spring AI 프로젝트: https://spring.io/projects/spring-ai/
- Tool Calling: https://docs.spring.io/spring-ai/reference/api/tools.html
- PGvector: https://docs.spring.io/spring-ai/reference/api/vectordbs/pgvector.html
- Spring Initializr: https://start.spring.io/
