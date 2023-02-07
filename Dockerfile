FROM maven:3.8.6-jdk-11-slim as builder
COPY src /usr/src/app/src
COPY pom.xml /usr/src/app
RUN mvn -f /usr/src/app/pom.xml -Dmaven.test.skip package spring-boot:repackage

FROM adoptopenjdk/openjdk11:jre-11.0.11_9
ENV TZ="Europe/Moscow"
RUN date
COPY --from=builder /usr/src/app/target/javaproTeams30Backend-1.0-SNAPSHOT.jar social-network.jar
CMD java -jar social-network.jar
