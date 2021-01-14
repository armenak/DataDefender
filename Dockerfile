#
# Build stage
#
FROM maven:3.6.0-jdk-11-slim AS datadefender-build
COPY . /build
WORKDIR /build
RUN mvn -f /build/pom.xml dependency:resolve && mvn -f /build/pom.xml package -Djdbc-drivers-all

#
# Package stage
#
FROM openjdk:11-jre-slim as datadefender
COPY --from=datadefender-build /build/target  /usr/local/datadefender
RUN chmod +x /usr/local/datadefender/datadefender.docker

