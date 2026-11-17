# 공개 API 부하 테스트

비로그인 사용자도 호출할 수 있는 API(`SecurityConfig`의 `permitAll`)가 요청이 몰렸을 때 얼마나 버티는지 확인한다.
개선(N+1 해결, rate limit 등) 전후에 **같은 데이터, 같은 시나리오**로 측정해서 비교한다.

## 파일 구성

| 파일 | 역할 |
|---|---|
| `../docker-compose.loadtest.yml` | 백엔드 컨테이너에 운영(EC2 t3.micro)과 같은 제한(메모리 700m, CPU 2개)을 덧씌움 |
| `seed.sql` | 테스트 데이터 (기수 901~905, FAQ `[LT]` 표시) |
| `cleanup.sql` | `seed.sql`로 넣은 데이터만 삭제 |
| `smoke.js` | 가상 사용자 1명으로 대상 API가 200을 주는지 확인 |
| `single-api.js` | API 하나에 가상 사용자 10 → 50 → 100 → 200명, 단계마다 1분 |
| `mixed.js` | FAQ 50% / 프로젝트 목록 30% / 블로그 목록 20%를 섞어서 호출 |
| `lib/common.js` | 단계별 scenario, 판정 기준(p95 < 1초, 실패율 < 1%), 결과 저장 |
| `results/` | 테스트 결과 JSON (k6 `handleSummary`가 저장) |

## 실행 순서

```bash
# 0. k6 설치 (최초 1회)
brew install k6

# 1. 운영과 같은 제한으로 서버 실행
docker compose -f docker-compose.yml -f docker-compose.loadtest.yml up -d --build

# 2. 테스트 데이터 넣기 (로컬 DB 전용)
set -a; source .env; set +a
docker exec -i chunghaha-db mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" < loadtest/seed.sql

# 3. 스모크 테스트
k6 run loadtest/smoke.js

# 4. 단일 API 테스트 (테스트 사이에 1분 정도 쉬기)
k6 run -e TARGET=/api/admin/faqs loadtest/single-api.js        # 기준선
k6 run -e TARGET=/api/projects loadtest/single-api.js
k6 run -e TARGET=/api/blogs loadtest/single-api.js
k6 run -e TARGET=/api/gallery/projects loadtest/single-api.js

# 5. 혼합 테스트
k6 run loadtest/mixed.js

# 테스트 중 다른 터미널에서 서버 자원 확인
docker stats chunghaha-backend

# 6. 정리
docker exec -i chunghaha-db mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" < loadtest/cleanup.sql
docker compose -f docker-compose.yml -f docker-compose.loadtest.yml down
```

선택 옵션: `-e VU_LEVELS=10,50,100,200`, `-e STAGE_DURATION=1m`, `-e THINK_TIME=1` (요청 사이 대기 초)

## 결과 해석 시 주의

- 로컬은 t3 CPU 크레딧 제한이 없고 DB가 같은 PC에 있어서 **운영보다 좋게 나온다** → "최대치"로 해석
- 가상 사용자는 요청 후 1초 쉬므로, 가상 사용자 수 ≈ 초당 요청 수의 상한
- 부하 생성기(k6)와 서버가 같은 PC의 CPU를 나눠 쓴다

## 결과 기록

### 개선 전 부하 테스트 (2026-11-11, `develop` 3d704ee)

200명 단계 기준. 판정 기준은 p95 < 1,000ms, 실패율 < 1%.

| API | p95 (ms) | 초당 처리 (최대 200) | 최대 CPU | 최대 메모리 | 판정 |
|---|---|---|---|---|---|
| `GET /api/projects` | **1,488** | **106.9** | 216% | 664MB | ❌ 기준 초과 |
| `GET /api/blogs` | 94 | 195.9 | 49% | 656MB | ✅ |
| `GET /api/gallery/projects` | 61 | 198.2 | 37% | 656MB | ✅ |
| `GET /api/admin/faqs` (기준선) | 29 | 200.0 | 44% | 580MB | ✅ |
| 혼합 (FAQ 50 / 프로젝트 30 / 블로그 20) | 114 | 192.0 | 205% | 655MB | ✅ (max 2.4초 섞임) |

- 모든 테스트에서 실패한 요청은 0건, 컨테이너 재시작 0회
- `GET /api/projects`는 100명부터 p95가 590ms로 오르기 시작했고, 200명에서 CPU 2개를 모두 쓰면서 처리량이 초당 약 107건에서 멈춤 (N+1: 목록 1번에 쿼리 301번, 응답 약 264KB)
- 혼합 테스트에서 FAQ의 p95는 25ms로 낮았지만, 200명 단계에서 최대 2초 응답이 섞임 → 무거운 API의 영향이 가벼운 API에 번지기 시작
- 나머지 API는 200명까지 여유가 있어서, 진짜 한계를 알려면 스트레스 테스트(`-e VU_LEVELS=300,500,800,1000` 또는 `-e THINK_TIME=0`)가 필요

### 개선 전 스트레스 테스트 (2026-11-16, `develop` 3d704ee)

가상 사용자 300 → 500 → 800 → 1000명, 단계마다 1분. 나머지 조건은 부하 테스트와 같다.

```bash
k6 run -e VU_LEVELS=300,500,800,1000 -e TARGET=/api/projects loadtest/single-api.js
k6 run -e VU_LEVELS=300,500,800,1000 loadtest/mixed.js
```

1000명 이상은 macOS 기본 파일 열기 제한에 걸릴 수 있어서 먼저 `ulimit -n 10240`을 실행한다.

**p95 응답 시간 (ms)**

| API | 300명 | 500명 | 800명 | 1000명 |
|---|---|---|---|---|
| `GET /api/projects` | **4,731** | **6,690** | **9,610** | **11,284** |
| 혼합 | 486 | **1,497** | **3,513** | **4,911** |
| `GET /api/blogs` | 93 | 82 | 643 | **2,021** |
| `GET /api/gallery/projects` | 70 | 188 | 106 | 459 |
| `GET /api/admin/faqs` | 63 | 68 | 186 | 87 |

**초당 처리량** (단계별 요청 수 ÷ 60초, 대략값. 1000명이면 최대 약 1000)

| API | 300명 | 500명 | 800명 | 1000명 |
|---|---|---|---|---|
| `GET /api/projects` | **97** | **102** | **109** | **113** |
| 혼합 | 277 | 307 | 286 | 287 |
| `GET /api/blogs` | 295 | 485 | 720 | **585** |
| `GET /api/gallery/projects` | 290 | 467 | 774 | 908 |
| `GET /api/admin/faqs` | 285 | 488 | 755 | 960 |

**최대 CPU / 메모리**

| API | 최대 CPU | 최대 메모리 |
|---|---|---|
| `GET /api/projects` | 216% | 698.7MB |
| 혼합 | 249% | 698.8MB |
| `GET /api/blogs` | 260% | 696.3MB |
| `GET /api/gallery/projects` | 279% | 697.8MB |
| `GET /api/admin/faqs` | 158% | 670.8MB |

- 모든 테스트에서 실패한 요청 0건, 컨테이너 재시작 0회, OOM 0회
- `GET /api/projects`의 한계는 초당 약 100~110건. 사용자를 늘려도 처리량은 그대로고 응답 시간만 늘어남 (요청이 Tomcat 대기열에서 기다림)
- 혼합에서는 서버 전체가 초당 약 290건에서 멈춤. 혼자서는 1000명에서도 p95 87ms였던 FAQ가 p95 3.5초까지 느려짐 → 프로젝트 목록이 서버 전체의 한계를 정함
- `GET /api/blogs`는 800~1000명 사이에서 한계. 1000명에서는 처리량이 오히려 줄어듦 (720 → 585). 페이지네이션이 없어 응답이 크고, 테스트 동안 받은 데이터가 12GB
- FAQ와 갤러리는 1000명까지 통과. 다만 1000명 단계에서는 k6도 같은 PC의 CPU를 많이 써서, 서버의 한계가 아니라 측정 범위의 한계일 수 있음
- 메모리가 제한(700MB)에 거의 닿음. 테스트 후에도 673MB로 여유 약 26MB (테스트 전 642MB)
- 실패가 0건인 것은 k6가 응답을 60초까지 기다리기 때문. 운영에서는 nginx가 60초가 지나면 504를 준다

### 테스트 중 발견

- `/api/health`는 `SecurityConfig`에 permitAll로만 등록돼 있고 컨트롤러가 없음. 없는 경로 요청이 404가 아닌 500으로 응답되고, 매번 스택 트레이스가 로그에 남음

### 개선 후

(N+1 해결, rate limit 적용 후 같은 시나리오로 측정해서 채운다)
