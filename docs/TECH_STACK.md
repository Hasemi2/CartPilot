# CartPilot 기술 스택 및 실행 환경

## 1. 기술 목표

CartPilot은 자연어 요청을 외부 LLM이 구조화된 Tool 호출로 변환하고, Java application service가 실제 상품·재고·장바구니·주문 규칙을 검증하는 AI 커머스 학습 프로젝트다.

로컬 실행은 설치가 필요 없는 H2와 Stub AI adapter를 기본으로 한다. PostgreSQL과 pgvector는 GitHub Actions의 분리된 검증 환경에서 확인한다.

## 2. 핵심 스택

| 영역 | 기술 | 기본 환경 (`local-lite`) | 검증 환경 (`ci-postgres`) |
|---|---|---|---|
| 언어 | Java 21 | 동일 | 동일 |
| Backend | Spring Boot 4.1.2, Spring MVC | 동일 | 동일 |
| Build | Gradle 9.8.2 Wrapper, Groovy DSL | 동일 | 동일 |
| ORM | Spring Data JPA / Hibernate | H2 adapter | PostgreSQL adapter |
| 관계형 DB | H2 file database / PostgreSQL 18.6 | H2 | PostgreSQL |
| Migration | Flyway | 공통 migration | 공통 + PostgreSQL 전용 migration |
| AI framework | Spring AI 2.0.2 | Stub 또는 선택적 외부 LLM | Stub ChatModel |
| Tool orchestration | Spring AI ChatClient / Tool Calling | 결정적 Stub 테스트와 AI 데모 | DB와 분리된 Tool 테스트 |
| 벡터 검색 | SimpleVectorStore / PgVectorStore | SimpleVectorStore | pgvector |
| 결제 | 자체 Mock PG | 동일 | 동일 |
| 테스트 | JUnit 5, Spring Boot Test | H2 통합 테스트 | PostgreSQL 통합·동시성 테스트 |
| CI | GitHub Actions | push만 수행 | Linux runner + PostgreSQL service |
| 인프라 | H2 / PostgreSQL service | Docker 불필요 | GitHub Actions service container |
| 관측성 | Spring Actuator, Micrometer | 후반 도입 | Tool·LLM·결제 지표 검증 |
| 캐시 | Redis | 사용하지 않음 | 명확한 요구가 생길 때만 도입 |
| Frontend | React + TypeScript | MVP 후반 선택 | 동일 |

## 3. 구현 상태와 예정 의존성

### 현재 구성됨

- Java 21 toolchain
- Spring Boot 4.1.2
- Spring MVC
- Spring Data JPA
- H2 runtime과 `local-lite` 기본 프로필
- PostgreSQL JDBC driver
- JUnit 5 / Spring Boot Test
- Gradle Wrapper 9.8.2

### 다음 단계에서 추가

- Flyway core와 PostgreSQL 지원 모듈
- `application-local-ai.yml`, `application-ci-postgres.yml`
- Spring AI 2.0.2 BOM과 모델 starter
- Stub ChatModel adapter
- GitHub Actions PostgreSQL workflow

### RAG 단계에서 추가

- Spring AI SimpleVectorStore
- embedding model adapter
- PostgreSQL pgvector extension과 PgVectorStore

`build.gradle`의 정확한 starter 이름과 전이 의존성은 구현 시 Spring Boot와 Spring AI 공식 문서로 다시 확인한다. 사용하지 않는 라이브러리를 미리 추가하지 않는다.

## 4. 실행 프로필 명세

### `local-lite`

- 목적: 기본 로컬 개발
- DB: H2 file mode
- LLM: 자체 Stub ChatModel
- VectorStore: 비활성
- 외부 API 키: 불필요
- Docker: 불필요

권장 JDBC URL:

```text
jdbc:h2:file:./.data/cartpilot;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_ON_EXIT=FALSE
```

H2 PostgreSQL mode는 개발 편의 기능일 뿐 PostgreSQL 호환성을 보장하지 않는다.

### `local-ai`

- 목적: 실제 자연어 Tool 선택과 작은 RAG 데모
- DB: H2 file mode
- LLM: 환경변수로 선택한 외부 API
- VectorStore: SimpleVectorStore
- 비밀값: 로컬 환경변수로만 제공

### `ci-postgres`

- 목적: 실제 DB 동작 검증
- DB: GitHub Actions PostgreSQL service
- LLM: Stub ChatModel
- VectorStore: 기본 비활성, RAG job에서만 pgvector
- 데이터: 합성 테스트 데이터만 사용

## 5. 데이터베이스 규칙

- JPA 엔티티와 application service는 실행 프로필에 의존하지 않는다.
- money는 `BigDecimal`과 명시적인 currency로 표현한다.
- Flyway를 schema 변경의 단일 기준으로 사용한다.
- 공통 migration은 H2와 PostgreSQL에서 실행 가능한 단순 SQL을 우선한다.
- PostgreSQL 전용 인덱스, 잠금 보조 구조, pgvector migration은 분리한다.
- H2에서 통과한 쿼리도 PostgreSQL CI에서 반드시 다시 실행한다.
- 재고 동시성, 유니크 제약, 멱등성은 PostgreSQL 테스트 결과만 완료 근거로 인정한다.

## 6. AI와 Tool Calling 규칙

Spring AI의 `ChatClient`를 기본 오케스트레이션 API로 사용한다. LLM은 Tool 이름과 인자를 선택하며, Tool 실행과 반복 호출은 서버가 관리한다.

초기 Tool:

- `searchProducts(query, filters)`
- `getProductDetails(productId)`
- `checkStock(productId, quantity)`
- `addCartItem(productId, quantity)`
- `getCart()`
- `getOrderQuote()`
- `getMyOrderStatus(orderId)`

보안 규칙:

- 사용자 ID와 tenant 정보는 모델 인자가 아닌 server-side Tool Context에서 전달한다.
- Tool 입력 DTO에 필수값, 길이, 수량, 허용 필터를 명시한다.
- LLM이 전달한 가격, 재고, 사용자 ID를 신뢰하지 않는다.
- Tool은 repository를 직접 호출하지 않는다.
- 주문 확정과 결제 승인은 인증된 별도 REST endpoint로만 수행한다.
- Tool 인자·결과 전체를 기본 로그로 남기지 않는다.

## 7. RAG 기술 명세

RAG는 상품 설명, 사용 가이드, 배송·교환 정책처럼 비정형 문서를 검색하기 위한 후반 기능이다.

### 로컬 학습

`SimpleVectorStore`를 사용한다. 메모리 기반 유사도 검색과 JSON 저장을 이용할 수 있지만 영속성 검증의 기준으로 사용하지 않는다.

### PostgreSQL 검증

pgvector와 Spring AI PgVectorStore를 사용한다. 문서 chunk에는 다음 metadata를 포함한다.

- `documentId`
- `productId` 또는 `policyType`
- `source`
- `version`
- `updatedAt`

RAG 검색 결과는 설명 근거로만 사용한다. 가격, 가용 재고, 주문 상태는 Tool을 통해 live domain API에서 다시 조회한다.

## 8. 테스트 전략

| 테스트 | 환경 | 검증 대상 |
|---|---|---|
| 단위 테스트 | JVM | 도메인 규칙, 금액, 상태 전이 |
| MVC slice | JVM | 요청·응답, 입력 검증 |
| H2 통합 테스트 | `local-lite` | JPA mapping, 기본 repository, Tool 흐름 |
| Stub AI 테스트 | `local-lite` | Tool dispatch, Tool Context, 오류 처리 |
| PostgreSQL 통합 테스트 | GitHub Actions | 실제 migration, 제약조건, SQL |
| 동시성 테스트 | PostgreSQL | 재고 음수 방지, 결제 멱등성 |
| 실제 LLM smoke test | 허용된 환경 | 자연어 Tool 선택과 구조화 인자 |
| RAG 평가 | 두 VectorStore | 검색 관련성, 출처, 환각 여부 |

실제 LLM 테스트는 비용과 비결정성 때문에 기본 `test` task에서 분리한다.

## 9. 도입 순서

1. H2 `local-lite` 프로필과 Flyway
2. 상품 검색·재고 조회·장바구니 수직 슬라이스
3. Stub ChatModel 기반 Tool Calling
4. 허용된 환경에서 실제 LLM adapter
5. GitHub Actions PostgreSQL 통합 테스트
6. 주문·Mock 결제와 PostgreSQL 동시성 검증
7. SimpleVectorStore 기반 RAG 데모
8. pgvector 기반 검색 검증
9. 인증·관측성·부하 테스트

## 10. 버전 정책

- 2026-11-17 기준 프로젝트 고정 버전은 Spring Boot 4.1.2, Spring AI 2.0.2다.
- Spring AI 2.0.x는 Spring Boot 4.0.x와 4.1.x를 지원하는 조합을 사용한다.
- H2와 주요 Spring dependency 버전은 Spring Boot dependency management를 우선한다.
- PostgreSQL은 검증된 18.6 이미지를 CI와 선택적 Compose 환경에서 사용한다.
- milestone, release candidate, snapshot 버전은 기본 의존성으로 사용하지 않는다.
- 모델 이름은 코드에 고정하지 않고 환경설정으로 분리한다.

## 11. 공식 참고자료

- Spring Boot SQL Databases: https://docs.spring.io/spring-boot/reference/data/sql.html
- Spring Boot Database Initialization: https://docs.spring.io/spring-boot/how-to/data-initialization.html
- Spring AI Getting Started: https://docs.spring.io/spring-ai/reference/getting-started.html
- Spring AI Tool Calling: https://docs.spring.io/spring-ai/reference/api/tools.html
- Spring AI Vector Databases: https://docs.spring.io/spring-ai/reference/api/vectordbs.html
- Spring AI PgVector: https://docs.spring.io/spring-ai/reference/api/vectordbs/pgvector.html
- GitHub Actions PostgreSQL Service: https://docs.github.com/actions/tutorials/use-containerized-services/create-postgresql-service-containers
