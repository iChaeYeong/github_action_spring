# =========================
# 1) Build stage: Gradle + JDK21
# =========================
FROM gradle:8.7-jdk21 AS builder
WORKDIR /workspace

# (1) Gradle 관련 파일 먼저 복사 (캐시 효율)
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle


# (2) 소스 복사
COPY . .

# (3) 실행 권한 (리눅스 컨테이너라 필요)
RUN chmod +x gradlew

# (4) Spring Boot Jar 빌드
RUN ./gradlew clean build -x test

# =========================
# 2) Run stage: JRE 21 (가벼움)
# =========================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# builder에서 만든 jar를 app.jar로 복사
COPY --from=builder /workspace/build/libs/*.jar app.jar

EXPOSE 8080

# 컨테이너 실행 시 java -jar
ENTRYPOINT ["java","-jar","/app/app.jar"]