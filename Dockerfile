# syntax=docker/dockerfile:1
# Imagem JVM de um módulo da suite. O build roda a partir da raiz, porque os
# serviços dependem da common-library:
#   docker build --build-arg MODULE=ms-consent -t personalbank/ms-consent .

# --- ESTÁGIO 1: BUILD ---
FROM maven:3.9-eclipse-temurin-21 AS build
ARG MODULE
WORKDIR /src
COPY . .
# O cache do Maven fica num volume do BuildKit e é reaproveitado entre builds.
# sharing=locked evita que builds paralelos do compose escrevam nele ao mesmo tempo.
RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    mvn -B -q -pl ${MODULE} -am package -DskipTests

# --- ESTÁGIO 2: RUNTIME ---
FROM eclipse-temurin:21-jre
ARG MODULE
WORKDIR /app
# Formato "fast-jar" do Quarkus: quarkus-run.jar mais as pastas lib/, app/ e quarkus/
COPY --from=build /src/${MODULE}/target/quarkus-app/ ./
USER 1000
CMD ["java", "-jar", "quarkus-run.jar"]
