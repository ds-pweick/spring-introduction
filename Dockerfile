FROM maven:3-eclipse-temurin-17-alpine AS maven

WORKDIR /build

COPY . .

RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine

ARG UID=1000
ARG GID=1000

WORKDIR /app

COPY --chmod=755 --from=maven /build/app/target/app*.jar app.jar

USER ${UID}:${GID}

ENTRYPOINT ["java","-jar","app.jar"]