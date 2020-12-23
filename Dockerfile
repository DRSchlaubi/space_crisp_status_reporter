FROM openjdk:15 as builder
COPY . .
RUN ./gradlew installDist -Dorg.gradle.daemon=false

FROM adoptopenjdk/openjdk15-openj9

WORKDIR /usr/app
COPY --from=builder build/install/space_crisp_status_reporter/ .

ENTRYPOINT ["/usr/app/bin/space_crisp_status_reporter"]
