# syntax=docker/dockerfile:1

FROM gradle:9.2.1-jdk21 AS build
WORKDIR /home/gradle/project

COPY --chown=gradle:gradle server/gradle /home/gradle/project/server/gradle
COPY --chown=gradle:gradle server/gradlew /home/gradle/project/server/gradlew
COPY --chown=gradle:gradle server/build.gradle.kts /home/gradle/project/server/build.gradle.kts
COPY --chown=gradle:gradle server/settings.gradle.kts /home/gradle/project/server/settings.gradle.kts
COPY --chown=gradle:gradle server/detekt.yml /home/gradle/project/server/detekt.yml
COPY --chown=gradle:gradle server/mise.toml /home/gradle/project/server/mise.toml
COPY --chown=gradle:gradle specs /home/gradle/project/specs
COPY --chown=gradle:gradle server/src /home/gradle/project/server/src

WORKDIR /home/gradle/project/server
RUN --mount=type=cache,target=/home/gradle/.gradle \
    gradle bootJar -x test --no-daemon

FROM eclipse-temurin:21-jre
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /home/gradle/project/server/build/libs/*.jar /app/app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
