# syntax=docker/dockerfile:1
FROM eclipse-temurin:25-jdk AS builder
WORKDIR /app
COPY gradle/ gradle/
COPY gradlew build.gradle.kts settings.gradle gradle.properties ./
RUN chmod +x gradlew
COPY src/ src/
COPY resources/ resources/
COPY tests/ tests/
COPY psi-tests/ psi-tests/
COPY gradle-tests/ gradle-tests/
RUN --mount=type=cache,target=/root/.gradle ./gradlew --no-daemon check buildPlugin

FROM scratch AS artifact
COPY --from=builder /app/build/distributions/ /
