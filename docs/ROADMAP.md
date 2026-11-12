# CartPilot 로드맵

## 0. 프로젝트 초기화

- [x] Java 21 / Spring Boot 4.1.2 / Gradle Wrapper
- [x] 최소 실행 애플리케이션과 health smoke test
- [x] PostgreSQL Docker Compose와 환경변수 예시
- [x] AI Tool Calling 경계를 포함한 아키텍처 문서
- [x] 로컬 JDK 21 빌드·테스트 검증

완료 기준: `gradlew test`와 `gradlew build`가 성공하고 로컬 실행 절차가 문서화되어 있다.

## 1. Commerce 최소 수직 슬라이스

- 상품 조회와 게시 상태
- 재고 조회
- 인증 사용자 장바구니 추가·수정·삭제
- Flyway 초기 스키마와 소량의 개발용 데이터
- Testcontainers 기반 PostgreSQL 통합 테스트

완료 기준: REST API와 DB 테스트로 상품 ID, 현재 가격, 재고, 소유권 검증을 재현한다.

## 2. AI Tool Calling

- Spring AI 2.0.2와 환경변수 기반 모델 설정
- `searchProducts`, `getProductDetails`, `checkStock` Tool
- `addCartItem`, `getCart` Tool
- 인증 컨텍스트 기반 사용자 식별
- Tool 입력 제한, 감사 로그, 프롬프트 인젝션 방어 테스트

완료 기준: 자연어 요청이 실제 DB 상품 ID를 근거로 검색되고, LLM이 가격이나 사용자 ID를 만들어 내도 서버가 거부한다.

## 3. 주문과 Mock 결제

- 서버 계산 견적과 만료 정책
- 별도 인증 endpoint를 통한 명시적 주문 확인
- 재고 예약·해제와 동시성 테스트
- Mock PG, 결제 멱등키, 타임아웃, 중복 콜백, 상태 대사

완료 기준: 100개 동시 요청에서도 초과 판매와 중복 결제가 없고, 모호한 결제 결과를 재조회할 수 있다.

## 4. RAG 상품 탐색

- PostgreSQL pgvector 확장과 Spring AI PgVectorStore
- 상품 설명·가이드·배송/교환 정책 임베딩
- 키워드 검색과 의미 검색 평가 세트
- RAG 결과의 상품 ID를 live catalog API로 재검증

완료 기준: 근거 출처와 검색 품질을 비교할 수 있고, 가격·재고는 항상 거래 API에서 조회한다.

## 5. 안정화와 선택 기능

- 인증·인가 강화, 구조화된 오류, 관측성, k6 부하 테스트
- 실제 사용 사례가 확인된 경우에만 Redis 도입
- 선택적으로 React UI와 읽기 전용 MCP Server 추가

완료 기준: 보안·동시성·장애 시나리오의 재현 가능한 테스트 결과와 남은 제한사항을 문서화한다.
