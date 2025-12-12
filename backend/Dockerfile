# ----------------------------------------------------------------------
# STAGE 1: BUILD (Java 21 + Maven)
# ----------------------------------------------------------------------
# We use a specific Maven image that comes with JDK 21
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /project

# Copy just the pom.xml first to cache dependencies
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -B -DskipTests dependency:go-offline

# Copy source and build
COPY src src
RUN --mount=type=cache,target=/root/.m2 mvn -B -DskipTests -Dquarkus.package.type=fast-jar package

# ----------------------------------------------------------------------
# STAGE 2: RUN (Java 21 JRE only)
# ----------------------------------------------------------------------
# We switch to a lightweight JRE image for the final container
FROM eclipse-temurin:21-jre AS final

WORKDIR /app

# Copy the build artifacts from the 'build' stage
# Quarkus 'fast-jar' layout is the default in recent versions
COPY --from=build /project/target/quarkus-app/lib/ /app/lib/
COPY --from=build /project/target/quarkus-app/*.jar /app/
COPY --from=build /project/target/quarkus-app/app/ /app/app/
COPY --from=build /project/target/quarkus-app/quarkus/ /app/quarkus/

EXPOSE 8080
CMD ["java", "-jar", "/app/quarkus-run.jar"]