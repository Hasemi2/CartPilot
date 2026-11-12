# CartPilot

CartPilot은 자연어 상품 탐색부터 추천, 장바구니, 주문 준비까지 연결하는 AI 커머스 학습 프로젝트입니다. AI는 사용자의 의도를 해석하고 제한된 Tool을 선택하며, 가격·재고·소유권·주문·결제의 최종 판단은 서버가 담당합니다.

## 현재 상태

- Java 21, Spring Boot 4.1.2, Gradle 9.8.2 기반 백엔드 초기화
- PostgreSQL 18.6 로컬 실행용 Docker Compose
- `/api/health` smoke endpoint와 MVC 테스트
- Spring AI 2.0.2는 AI Tool Calling 단계에서 추가 예정
- pgvector와 Redis는 실제 사용 사례가 생기는 단계에서 추가 예정

## 요구 사항

- JDK 21
- Docker 및 Docker Compose(로컬 PostgreSQL 실행 시)

Windows에서 기본 `JAVA_HOME`이 다른 버전을 가리킨다면 JDK 21로 변경한 뒤 명령을 실행합니다.

## 로컬 실행

```powershell
Copy-Item .env.example .env
docker compose --env-file .env -f infra/compose.yaml up -d
Set-Location backend
.\gradlew.bat bootRun
```

헬스 확인:

```powershell
Invoke-RestMethod http://localhost:8080/api/health
```

테스트:

```powershell
Set-Location backend
.\gradlew.bat test
```

## 핵심 설계 원칙

```text
사용자 요청
  -> Spring AI / LLM (의도 해석과 Tool 선택)
  -> agent Tool 어댑터 (입력 범위와 스키마 제한)
  -> 도메인 application service (권한·가격·재고·소유권 검증)
  -> repository / PostgreSQL (거래 데이터의 최종 근거)
```

LLM에는 결제 승인이나 주문 확정 Tool을 제공하지 않습니다. 주문 확정은 서버가 견적을 다시 검증한 뒤 별도의 인증된 API에서 사용자의 명시적 확인을 받아 수행합니다.

자세한 결정은 [아키텍처](docs/ARCHITECTURE.md), [로드맵](docs/ROADMAP.md), [기술 스택](docs/TECH_STACK.md)을 참고합니다.

