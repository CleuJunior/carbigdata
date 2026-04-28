FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY gradlew .
COPY gradle/ gradle/
COPY settings.gradle .
COPY build.gradle .
COPY api/build.gradle api/
COPY api/src/ api/src/
RUN chmod +x gradlew && ./gradlew :api:bootJar -x test --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/api/build/libs/carbigdata-occurrences.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
