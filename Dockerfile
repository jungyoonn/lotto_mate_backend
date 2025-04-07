# 1. 빌드 단계: Gradle 8.13을 사용하여 Java 애플리케이션 빌드
FROM gradle:8.13-jdk17 AS builder
WORKDIR /app
# Gradle 종속성만 먼저 복사하여 캐시 효율성 높이기
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
# 래퍼 스크립트가 있다면 복사
COPY gradlew ./
RUN chmod +x ./gradlew
# 종속성 다운로드
RUN gradle dependencies --no-daemon
# 소스 코드 복사 및 빌드
COPY src/ ./src/
RUN gradle build --no-daemon -x test

# 2. 실행 단계: JRE 환경으로 실행
FROM eclipse-temurin:17-jre
WORKDIR /app
# 빌드된 JAR 파일 복사 (build/libs 디렉토리에 생성)
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
# 스프링 애플리케이션 실행
CMD ["java", "-jar", "app.jar"]