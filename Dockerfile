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

ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar"]