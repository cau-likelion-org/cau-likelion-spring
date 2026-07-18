# CAU LikeLion Spring

중앙대 멋쟁이 사자처럼 공식 홈페이지 서버 레포지토리

## 기술 스택

- Java 17
- Spring Boot 4.1.0
- Spring Data JPA
- MySQL 8.4
- Gradle
- Docker / Docker Compose

## 로컬 실행 방법

### 1. 환경변수 파일 생성

프로젝트 루트의 `.env.example`을 복사하여 `.env`를 생성합니다.

```bash
cp .env.example .env
```

`.env` 파일을 열어 필요한 값을 채워 넣습니다.

```env
MYSQL_DATABASE=chunghaha
MYSQL_USER=chunghaha
MYSQL_PASSWORD=your_password
MYSQL_ROOT_PASSWORD=your_root_password
```

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

이후 IntelliJ에서 `Active profiles: local`로 애플리케이션을 실행하면 `localhost:3306`의 MySQL에 붙습니다.

## 패키지 구조

도메인 기준으로 분리되어 있으며, 각 도메인 내부는 `domain / repository / service / controller / dto` 계층으로 구성됩니다.

```text
com.example.cau_likelion_spring
 ├── global          # 공통 설정, 예외처리, BaseTimeEntity 등
 ├── organization     # Generation, Part, Platform
 ├── member           # Member, AllowedUserEmail
 ├── auth             # 로그인/인증 (구글 OAuth, JWT)
 ├── project          # 프로젝트 게시
 ├── gallery          # 활동 사진 게시판
 ├── session          # 세션 게시판
 ├── blog             # 외부 블로그 큐레이션
 ├── assignment       # 과제 관리
 ├── attendance       # 출결 관리
 ├── mypage           # 마이페이지 (조회 전용 파사드)
 └── notification     # 알림
```
