FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /build
COPY server/pom.xml server/pom.xml
COPY server/src server/src
COPY client client
WORKDIR /build/server
RUN mvn -B verify
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN mkdir -p /app/data && chown -R 10001:10001 /app
COPY --from=build --chown=10001:10001 /build/server/target/super-ace-online-13.0.0.jar /app/app.jar
USER 10001:10001
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
