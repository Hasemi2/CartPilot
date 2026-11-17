# CartPilot 로드맵

## 0. 프로젝트 초기화

- [x] Java 21 / Spring Boot 4.1.2 / Gradle Wrapper
- [x] 최소 실행 애플리케이션과 health smoke test
- [x] PostgreSQL 선택 실행용 Docker Compose 구성
- [x] AI Tool Calling 경계를 포함한 초기 아키텍처
- [x] 로컬 JDK 21 빌드·테스트 검증

Docker Compose는 선택적 PostgreSQL 실행 도구로만 유지한다.

## 0.5. 경량 로컬 개발 환경

- [x] H2 runtime dependency 추가
- [x] H2 file mode 기반 `local-lite` 프로필
- [ ] API 키가 필요 없는 Stub ChatModel 경계
- [ ] Flyway 공통 migration 구조
- [ ] PostgreSQL 전용 migration 분리 규칙
- [x] 로컬 실행 문서화
- [ ] Commerce 초기 데이터 문서화

완료 기준: Docker, PostgreSQL, LLM API 키 없이 `local-lite`로 애플리케이션과 테스트가 실행된다.

## 1. Commerce 최소 수직 슬라이스

- [ ] 게시된 상품 검색·상세 조회
- [ ] 현재 가격과 통화 표현
- [ ] 재고 조회
- [ ] 인증 사용자 장바구니 추가·수정·삭제·조회
- [ ] 개발용 합성 상품 데이터
- [ ] H2 repository·REST 통합 테스트
- [ ] GitHub Actions PostgreSQL service와 통합 테스트

완료 기준: 동일한 use case가 H2와 PostgreSQL에서 동작하고, 상품 ID·가격·재고·소유권을 서버가 검증한다.

## 2. AI Tool Calling

- [ ] Spring AI 2.0.2와 ChatClient 구성
- [ ] Stub ChatModel 기반 결정적 Tool dispatch 테스트
- [ ] `searchProducts`, `getProductDetails`, `checkStock` Tool
- [ ] `addCartItem`, `getCart` Tool
- [ ] 인증 컨텍스트 기반 사용자 식별
- [ ] Tool 입력 제한, 감사 가능성, 민감정보 로그 방지
- [ ] 허용된 환경에서 실제 LLM smoke test
- [ ] 프롬프트 인젝션과 잘못된 Tool 인자 테스트

완료 기준: API 키 없이 Tool 흐름을 재현할 수 있고, 실제 LLM 테스트에서는 자연어가 실제 상품 ID와 연결된다. LLM이 가격이나 사용자 ID를 만들어 내도 서버가 거부한다.

## 3. 주문과 Mock 결제

- [ ] 서버 계산 견적과 만료 정책
- [ ] 별도 인증 endpoint를 통한 명시적 주문 확인
- [ ] 재고 예약·해제
- [ ] Mock PG와 결제 멱등키
- [ ] 타임아웃, 중복 콜백, 상태 대사
- [ ] PostgreSQL 동시성 테스트

완료 기준: 실제 PostgreSQL에서 동시 요청에도 초과 판매와 중복 결제가 없고, 모호한 결제 결과를 재조회할 수 있다.

## 4. RAG 상품 탐색

- [ ] 상품 설명·가이드·배송/교환 정책 문서 모델
- [ ] embedding과 chunk metadata 규칙
- [ ] SimpleVectorStore 기반 로컬 데모
- [ ] 키워드 검색과 의미 검색 평가 세트
- [ ] pgvector/PgVectorStore 검증 환경
- [ ] 검색된 상품 ID를 live catalog API로 재검증

완료 기준: Docker 없는 로컬 환경에서도 RAG 흐름을 학습할 수 있고, pgvector 환경에서 영속성과 검색 품질을 별도로 검증한다. 가격·재고는 항상 거래 API에서 조회한다.

## 5. 안정화와 선택 기능

- [ ] 인증·인가 강화
- [ ] 구조화된 오류와 Tool 관측성
- [ ] k6 부하 테스트
- [ ] 실제 사용 사례가 확인된 경우에만 Redis 도입
- [ ] 선택적으로 React UI와 읽기 전용 MCP Server 추가

완료 기준: 보안·동시성·장애 시나리오의 재현 가능한 테스트 결과와 남은 제한사항을 문서화한다.
