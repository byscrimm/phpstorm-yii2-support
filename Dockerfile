# syntax=docker/dockerfile:1
FROM eclipse-temurin:25-jdk AS builder
WORKDIR /app
COPY gradle/ gradle/
COPY gradlew build.gradle.kts settings.gradle gradle.properties ./
COPY LICENSE.md NOTICE.md AUTHORS.md ./
RUN chmod +x gradlew
COPY src/ src/
COPY resources/ resources/
COPY tests/ tests/
COPY psi-tests/ psi-tests/
COPY gradle-tests/ gradle-tests/
ARG GRADLE_ARGS=
RUN --mount=type=cache,target=/root/.gradle,sharing=locked \
    ./gradlew --no-daemon --console=plain ${GRADLE_ARGS} check buildPlugin
RUN mkdir -p /export/reports \
    && cp build/distributions/*.zip /export/ \
    && cp -r build/reports/. /export/reports/ \
    && cp -r build/test-results /export/reports/ \
    && cp gradle.properties /export/build.properties \
    && cd /export && sha256sum *.zip > SHA256SUMS \
    && for archive in *.zip; do sha256sum "$archive" > "$archive.sha256"; done

FROM scratch AS artifact
COPY --from=builder /export/ /
