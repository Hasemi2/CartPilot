# CartPilot

CartPilot은 자연어 상품 탐색부터 추천, 장바구니, 주문 준비까지 연결하는 AI 커머스 프로젝트입니다. 외부 LLM은 사용자의 의도를 해석하고 제한된 Tool을 선택하며, 가격·재고·소유권·주문·결제의 최종 판단은 서버가 담당합니다.

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
| `local-ai` | H2 + Gemini 3.5 Flash Lite | 실제 LLM Tool Calling 데모 |
| `ci-postgres` | PostgreSQL + Stub ChatModel + 선택적 pgvector | GitHub Actions DB·벡터 검색 검증 |

`local-lite`는 기본 프로필이며 H2 파일 DB와 결정적 Stub ChatModel을 사용합니다. Stub은 고정 명령 형식으로 Tool Calling 흐름을 재현하며 LLM API 키가 필요하지 않습니다.

## 현재 상태

- Java 21, Spring Boot 4.1.2, Gradle 9.8.2 기반 백엔드 초기화
- `/api/health` smoke endpoint와 MVC 테스트
- H2 파일 DB 기반 `local-lite` 기본 프로필
- Flyway 기반 상품 스키마와 합성 상품 데이터
- 게시 상품 검색·상세·재고 확인과 장바구니 API
- OpenAPI 명세와 Swagger UI
- PostgreSQL Compose는 선택적 실행 도구로 유지
- Spring AI 2.0.2와 상품·재고·장바구니 Commerce Tool 5개
- `local-lite` 전용 Stub ChatModel과 Commerce Tool이 등록된 ChatClient
- `local-ai` 전용 Gemini 3.5 Flash Lite 연결과 공통 ChatClient
- SimpleVectorStore와 pgvector는 RAG 단계에서 도입 예정

## 요구 사항

- JDK 21
- 기본 빌드·테스트에는 Docker와 API 키가 필요하지 않음
- PostgreSQL 통합 테스트는 GitHub Actions 또는 허용된 별도 환경에서 실행

Windows에서 기본 `JAVA_HOME`이 다른 버전을 가리킨다면 JDK 21로 변경해야 합니다.

## 로컬 실행

별도 DB 서버 없이 기본 `local-lite` 프로필로 실행합니다.

```powershell
Set-Location backend
.\gradlew.bat bootRun
```

헬스 확인:

```powershell
Invoke-RestMethod http://localhost:8080/api/health
```

상품 검색:

```powershell
Invoke-RestMethod 'http://localhost:8080/api/products?query=키보드&maxPrice=100000'
```

`query`는 이름과 설명에서 부분 일치로 검색하며 최대 100자입니다. `maxPrice`는 선택값이며 0 이상의 금액만 허용합니다. 결과는 게시 상태인 상품 중 최대 20개이며, 가격은 `priceAmount`와 `currency`로 반환합니다.

`local-lite`에는 검색 흐름을 바로 확인할 수 있는 합성 상품 4개가 들어 있습니다. 게시 상품 3개와 필터 동작 검증용 미게시 상품 1개이며 실제 상품 정보가 아닙니다.

상품 상세 조회:

```powershell
Invoke-RestMethod 'http://localhost:8080/api/products/10000000-0000-0000-0000-000000000001'
```

상품 상세 조회는 게시 상품만 반환합니다. 미게시 상품과 존재하지 않는 상품은 모두 `404 Not Found`로 처리합니다.

상품 재고 확인:

```powershell
Invoke-RestMethod 'http://localhost:8080/api/products/10000000-0000-0000-0000-000000000001/availability?quantity=2'
```

수량은 1개 이상 100개 이하이며, 현재 재고로 요청 수량을 구매할 수 있는지를 `available`로 반환합니다. 이 API는 재고를 차감하거나 예약하지 않습니다.

장바구니 상품 수량 설정과 조회:

```powershell
$body = @{ quantity = 2 } | ConvertTo-Json
Invoke-RestMethod -Method Put -ContentType 'application/json' -Body $body `
  'http://localhost:8080/api/carts/me/items/10000000-0000-0000-0000-000000000001'

Invoke-RestMethod 'http://localhost:8080/api/carts/me'
```

상품 제거:

```powershell
Invoke-RestMethod -Method Delete `
  'http://localhost:8080/api/carts/me/items/10000000-0000-0000-0000-000000000001'
```

`PUT`은 같은 상품의 수량을 교체하므로 같은 요청을 반복해도 항목이나 수량이 중복 증가하지 않습니다. 장바구니에는 현재 가격이 표시되지만 주문 가격을 확정하지 않으며 재고를 예약하지도 않습니다. 현재 사용자 식별은 서버에 정의된 고정 학습용 컨텍스트를 사용하고, 인증 단계에서 실제 사용자 컨텍스트로 교체합니다.

주문 견적 계산:

```powershell
Invoke-RestMethod -Method Post 'http://localhost:8080/api/order-quotes'
```

주문 견적은 장바구니 상품의 현재 가격과 재고를 다시 확인해 총액과 주문 가능 여부를 계산합니다. 견적을 저장하거나 주문을 생성하지 않으며 재고도 예약하지 않습니다.

## Stub AI 대화 API

`local-lite`에서는 API 키 없이 Spring AI ChatClient와 Commerce Tool 호출 흐름을 확인할 수 있습니다.

```powershell
$body = @{ message = '상품 검색: 무선 키보드' } | ConvertTo-Json
Invoke-RestMethod -Method Post -ContentType 'application/json' -Body $body `
  'http://localhost:8080/api/agent/chat'
```

지원하는 Stub 명령은 다음과 같습니다.

- `상품 검색: 검색어`
- `상품 상세: 상품ID`
- `재고 확인: 상품ID, 수량`
- `장바구니 수량 설정: 상품ID, 수량`
- `장바구니 조회`
- `주문 견적`

Stub ChatModel은 자연어를 추론하지 않고 위 명령을 결정적인 Tool 호출로 변환합니다. 실제 자연어 해석은 `local-ai`에서 검증합니다.

## Gemini AI 실행

`local-ai`는 같은 H2·Flyway 데이터와 Commerce Tool을 사용하면서, Stub 대신 Gemini 3.5 Flash Lite가 자연어를 해석하고 호출할 Tool을 선택합니다. API 키는 설정 파일에 저장하지 않고 `GOOGLE_API_KEY` 환경변수로 전달합니다.

```powershell
$env:GOOGLE_API_KEY='발급받은 API 키'
Set-Location backend
.\gradlew.bat bootRun --args='--spring.profiles.active=local-ai'
```

실행 후 Swagger UI의 `POST /api/agent/chat`에 고정 명령 형식이 아닌 자연어를 입력할 수 있습니다.

```json
{
  "message": "10만원 이하 키보드를 찾아서 재고도 확인해줘"
}
```

Gemini는 Tool과 인자만 선택합니다. 실제 상품 검색, 가격, 재고, 장바구니 변경은 기존 application service가 검증하고 처리합니다. API 호출에는 모델 제공자의 사용량 정책이 적용됩니다.

Gemini 호출은 요청당 15초로 제한하고 Spring AI에서 최대 2회 시도합니다. 모델 과부하나 호출 시간 초과가 계속되면 HTTP `503 Service Unavailable`과 다음 응답을 반환합니다.

```json
{
  "code": "AI_MODEL_UNAVAILABLE",
  "message": "AI 모델이 일시적으로 혼잡합니다. 잠시 후 다시 시도해주세요."
}
```

## API 명세와 Swagger UI

애플리케이션 실행 후 브라우저에서 다음 주소를 엽니다.

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- OpenAPI YAML: http://localhost:8080/v3/api-docs.yaml

현재 명세에는 애플리케이션 상태 조회, 게시 상품 검색, 상품 상세 조회, 재고 확인, 장바구니, AI 대화 API가 포함됩니다. Swagger UI의 `Try it out`에서 활성 프로필의 데이터를 조회하고 Tool 호출을 실행할 수 있습니다.

## 테스트

```powershell
Set-Location backend
.\gradlew.bat test
```

## 선택적 PostgreSQL 실행

Docker 사용시 다음 Compose 파일로 PostgreSQL을 실행할 수 있습니다.

```powershell
Copy-Item .env.example .env
docker compose --env-file .env -f infra/compose.yaml up -d
Set-Location backend
.\gradlew.bat bootRun
```

실제 고객 데이터나 민감정보를 로컬 샘플, 외부 LLM, 원격 저장소, CI에 사용하지 않습니다.

자세한 결정은 [아키텍처](docs/ARCHITECTURE.md), [로드맵](docs/ROADMAP.md), [기술 스택](docs/TECH_STACK.md)을 참고합니다.
