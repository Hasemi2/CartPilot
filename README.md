# CartPilot

CartPilot은 자연어 상품 탐색부터 추천, 장바구니, 주문 준비까지 연결하는 AI 커머스 학습 프로젝트입니다. 외부 LLM은 사용자의 의도를 해석하고 제한된 Tool을 선택하며, 가격·재고·소유권·주문·결제의 최종 판단은 서버가 담당합니다.

## 핵심 구조

```text
사용자 요청
  -> Spring AI / 외부 LLM (의도 해석과 Tool 선택)
  -> agent Tool adapter (구조화 입력과 범위 제한)
  -> application service (권한·가격·재고·소유권 검증)
  -> repository / relational database
```

LLM에는 주문 확정이나 결제 승인 Tool을 제공하지 않습니다. 주문 확정은 별도의 인증된 API에서 사용자의 명시적 확인을 받은 후 서버가 가격과 재고를 다시 검증해 수행합니다.

## 개발 환경 전략

| 프로필 | 구성 | 용도 |
|---|---|---|
| `local-lite` | H2 + Stub ChatModel | 기본 개발·테스트 |
| `local-ai` | H2 + 외부 LLM + SimpleVectorStore | AI/RAG 데모 |
| `ci-postgres` | PostgreSQL + Stub ChatModel + 선택적 pgvector | GitHub Actions DB·벡터 검색 검증 |

H2 프로필과 AI adapter는 다음 구현 단계입니다. 현재 코드에는 아직 추가되지 않았으므로 현 시점의 `bootRun`은 PostgreSQL 연결이 필요합니다. 기본 Gradle 테스트는 DB 서버 없이 실행됩니다.

## 현재 상태

- Java 21, Spring Boot 4.1.2, Gradle 9.8.2 기반 백엔드 초기화
- `/api/health` smoke endpoint와 MVC 테스트
- Docker 없는 `local-lite` 목표 아키텍처 문서화
- PostgreSQL Compose는 선택적 실행 도구로 유지
- Spring AI 2.0.2, H2, Flyway는 다음 단계에서 추가 예정
- SimpleVectorStore와 pgvector는 RAG 단계에서 도입 예정

## 요구 사항

- JDK 21
- 기본 빌드·테스트에는 Docker와 API 키가 필요하지 않음
- PostgreSQL 통합 테스트는 GitHub Actions 또는 허용된 별도 환경에서 실행

Windows에서 기본 `JAVA_HOME`이 다른 버전을 가리킨다면 JDK 21로 변경해야 합니다.

## 테스트

```powershell
Set-Location backend
.\gradlew.bat test
```

## 선택적 PostgreSQL 실행

Docker를 사용할 수 있는 환경에서는 다음 Compose 파일로 PostgreSQL을 실행할 수 있습니다.

```powershell
Copy-Item .env.example .env
docker compose --env-file .env -f infra/compose.yaml up -d
Set-Location backend
.\gradlew.bat bootRun
```

실제 고객 데이터나 민감정보를 로컬 샘플, 외부 LLM, 원격 저장소, CI에 사용하지 않습니다.

자세한 결정은 [아키텍처](docs/ARCHITECTURE.md), [로드맵](docs/ROADMAP.md), [기술 스택](docs/TECH_STACK.md)을 참고합니다.
