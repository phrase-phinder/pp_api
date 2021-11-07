FROM maven:3.6.0-jdk-11-slim AS build
COPY src /src
COPY pom.xml pom.xml
RUN mvn clean package
EXPOSE 7070
ENTRYPOINT ["mvn","spring-boot:run"]
