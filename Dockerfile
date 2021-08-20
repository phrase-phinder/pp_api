FROM openjdk:8-jdk-alpine
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 7070
ENTRYPOINT ["java","-jar","/app.jar"]
