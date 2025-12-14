# ============================================
# Dockerfile.jvm - Multi-stage build for JVM
# src/main/docker/Dockerfile.jvm
# ============================================

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml and download dependencies (cache layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests -Dquarkus.package.jar.type=uber-jar

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Add non-root user
RUN addgroup -g 1001 quarkus && \
    adduser -D -u 1001 -G quarkus quarkus

# Copy jar from build stage
COPY --from=build --chown=quarkus:quarkus /app/target/*-runner.jar /app/application.jar

USER quarkus
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/application.jar"]

# ============================================
# Dockerfile.native - Native image build
# src/main/docker/Dockerfile.native
# ============================================

#FROM quay.io/quarkus/ubi-quarkus-mandrel-builder-image:jdk-21 AS build
#WORKDIR /app
#
#COPY --chown=quarkus:quarkus pom.xml .
#COPY --chown=quarkus:quarkus src ./src
#
#RUN mvn clean package -Pnative -DskipTests
#
#FROM quay.io/quarkus/quarkus-micro-image:2.0
#WORKDIR /app
#
#COPY --from=build --chown=1001:1001 /app/target/*-runner /app/application
#
#USER 1001
#EXPOSE 8080
#
#ENTRYPOINT ["/app/application"]