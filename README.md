# CAU LikeLion Spring

중앙대 멋쟁이 사자처럼 공식 홈페이지 서버 레포지토리

## 기술 스택

- Java 17
- Spring Boot 4.1.0
- Spring Data JPA
- MySQL 8.4
- Gradle
- Docker / Docker Compose
- AWS S3 (이미지/파일 업로드), Firebase Admin SDK (FCM 푸시 알림)

## 로컬 실행 방법

### 1. 환경변수 파일 생성

프로젝트 루트의 `.env.example`을 복사하여 `.env`를 생성합니다.

```bash
cp .env.example .env
```

`.env` 파일을 열어 필요한 값을 채워 넣습니다. DB 접속 정보 외에도 아래 값들이 필요합니다 - 특히 **JWT_SECRET, GOOGLE_CLIENT_ID, AWS_ACCESS_KEY/AWS_SECRET_KEY/AWS_S3_BUCKET은 기본값이 없어서 비워두면 애플리케이션 컨텍스트 로딩 자체가 실패합니다.**

```env
# MySQL
MYSQL_DATABASE=chunghaha
MYSQL_USER=chunghaha
MYSQL_PASSWORD=your_password
MYSQL_ROOT_PASSWORD=your_root_password

# JWT (32바이트 이상, 필수)
JWT_SECRET=

# 구글 로그인 (ID Token 검증용, 필수)
GOOGLE_CLIENT_ID=

# S3 (이미지/파일 업로드, 필수 - 값이 없으면 S3Uploader 빈 생성에 실패해 서버가 안 뜸)
AWS_ACCESS_KEY=
AWS_SECRET_KEY=
AWS_S3_BUCKET=

# Firebase (FCM 푸시 알림, 비워두면 알림 기능만 비활성화되고 서버는 정상 기동)
FIREBASE_CREDENTIALS_BASE64=

# Mail (모집 알림 발송용 Gmail SMTP, 비워두면 메일 발송 기능만 비활성화)
MAIL_PASSWORD=
```

전체 항목은 `.env.example`을 참고하세요.

`.env`는 Git에 커밋되지 않습니다 (`.gitignore`에 등록되어 있음). 각자 로컬에서만 생성해서 사용하세요.

### 2. Docker Compose 실행

DB와 백엔드를 함께 띄웁니다.

```bash
docker compose up -d --build
```

- 최초 실행 시 MySQL 이미지 다운로드 → 백엔드 이미지 빌드 → MySQL 컨테이너 기동 → health check 통과 → 백엔드 컨테이너 기동 순서로 진행됩니다.
- 코드를 수정한 뒤에는 다시 `--build` 옵션으로 재실행해야 반영됩니다.

### 3. 실행 상태 확인

```bash
docker compose ps
```

`db`, `backend` 두 컨테이너 모두 `Up`(또는 `Up (healthy)`) 상태면 정상입니다.

### 4. 백엔드 로그 확인

```bash
docker compose logs -f backend
```

아래 로그가 보이면 정상적으로 뜬 것입니다.

```text
Started CauLikelionSpringApplication
```

### 5. DB 테이블 확인 (선택)

```bash
docker compose exec db mysql -u chunghaha -p
```

비밀번호 입력 후:

```sql
USE chunghaha;
SHOW TABLES;
```

### 6. 종료

```bash
docker compose down
```

DB 데이터를 포함해 완전히 초기화하려면:

```bash
docker compose down -v
```

> `-v` 옵션은 MySQL 데이터가 저장된 볼륨까지 삭제합니다. 초기 설정을 처음부터 다시 검증할 때만 사용하세요.

## 로컬(IDE)에서 직접 실행하고 싶은 경우

매번 이미지를 리빌드하면 느리기 때문에, 평소 개발 중에는 DB만 도커로 띄우고 백엔드는 IntelliJ에서 `local` 프로필로 직접 실행하는 걸 추천합니다.

```bash
docker compose up db -d
```

이후 IntelliJ에서 `Active profiles: local`로 애플리케이션을 실행하면 `localhost:3306`(로컬 포트 충돌 시 `3307` 등으로 매핑)의 MySQL에 붙습니다.

> IntelliJ Run Configuration이 `.env` 파일을 자동으로 읽지 않는 경우가 있습니다 (EnvFile 플러그인 미설정 등). 서버가 `AWS_S3_BUCKET` 등 필수 값을 못 찾는다는 에러(`PlaceholderResolutionException`)로 안 뜨면, Run Configuration에 EnvFile 플러그인이 `.env`를 가리키도록 설정돼 있는지 먼저 확인하세요.

## 패키지 구조

도메인 기준으로 분리되어 있으며, 각 도메인 내부는 `domain / repository / service / controller / dto` 계층으로 구성됩니다.

```text
com.example.cau_likelion_spring
 ├── global          # 공통 설정, 예외처리, JWT 필터, 파일 업로드(S3Uploader), BaseTimeEntity 등
 ├── organization     # Generation(기수), Part(파트)
 ├── intro            # 소개/랜딩페이지 콘텐츠 (Track, Curriculum, DesiredTalent, Faq, Activity, Indicator, Roadmap)
 ├── member           # Member, AllowedUserEmail, FcmToken
 ├── auth             # 로그인/인증 (구글 OAuth, JWT, RefreshToken)
 ├── project          # 프로젝트 게시
 ├── gallery
 │    ├── project      # 갤러리 - 프로젝트
 │    ├── history      # 갤러리 - 추억
 │    └── session      # 갤러리 - 세션
 ├── blog             # 외부 블로그 큐레이션
 ├── assignment       # 과제 관리 (운영진/아기사자 컨트롤러 분리)
 ├── attendance       # 출결 관리
 ├── mypage           # 마이페이지 (상벌점 계산 등 조회 전용 파사드)
 └── notification     # 모집 알림 신청, 이메일 발송
```

## 이미지/파일 업로드

- `POST /api/files/{domain}`으로 업로드하며, `{domain}`은 `UploadDomain` enum 값(대문자, 예: `PROJECT`, `ROADMAP`, `ASSIGNMENT`)을 그대로 사용합니다.
- 도메인별 허용 확장자/최대 용량은 `global/util/UploadDomain.java`에 정의되어 있습니다.
- `PROJECT`, `HISTORY`, `SESSION`, `ACTIVITY`, `ROADMAP` 도메인의 `jpg/jpeg/png/webp` 이미지는 업로드 시 서버에서 자동으로 리사이징됩니다 (긴 변 1920px, 이미 그보다 작으면 원본 유지). 리사이징 로직은 `global/util/S3Uploader.java` 참고.
- `ASSIGNMENT`(과제 첨부파일)는 리사이징 대상이 아니며 원본 그대로 업로드됩니다.

## 배포 환경 참고사항

- 배포 대상 인스턴스는 AWS EC2 t3.micro(메모리 1GB)로, JVM 힙(`-Xmx400m`, `Dockerfile`)과 컨테이너 메모리 한도(`mem_limit: 700m`, `docker-compose.prod.yml`)를 명시적으로 제한해 nginx/OS가 쓸 메모리를 보호하고 있습니다.
- EC2에 1GB 스왑을 추가로 구성해 메모리 부족 상황에서도 프로세스가 즉시 강제 종료되지 않도록 안전망을 두었습니다 (레포지토리 외부 설정, `/etc/fstab` 참고).
- nginx `client_max_body_size`도 Spring의 `multipart` 설정(현재 22MB)보다 크거나 같게 맞춰야 합니다.

