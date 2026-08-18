# 1단계: Gradle로 Spring Boot JAR 빌드
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app

COPY gradlew .
COPY gradle ./gradle
COPY build.gradle .
COPY settings.gradle .

RUN chmod +x gradlew

# 소스 복사
COPY src ./src

# 테스트를 제외하고 실행 가능한 JAR 생성
RUN ./gradlew clean bootJar --no-daemon -x test


# 2단계: 생성된 JAR만 실행
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

# 컨테이너 메모리 한도(docker-compose.prod.yml의 mem_limit)보다 항상 작게 유지해야 함 -
# 힙(-Xmx) 밖에서 쓰는 메타스페이스/스레드 스택/AWS SDK 통신 버퍼 등을 위한 여유를 남겨둔다.
# 이 상한이 없으면 JVM이 계속 늘어나다 컨테이너 한도를 넘겨 리눅스 OOM Killer에 의해
# 프로세스가 예외 없이 강제 종료될 수 있다 (애플리케이션 레벨의 정상적인 OutOfMemoryError로 막기 위함).
ENTRYPOINT ["java", "-Xmx400m", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar"]