FROM maven:3.5-jdk-8-alpine as builder
WORKDIR /app
COPY src/ ./src
COPY pom.xml ./
RUN mvn clean install


FROM openjdk:8-jdk-alpine
WORKDIR /app
RUN apk add --no-cache tzdata
COPY --from=builder /app/target/task-scheduler-0.0.1-SNAPSHOT.jar /app/task-scheduler.jar
ENTRYPOINT ["java","-jar","-Dspring.profiles.active=dev","/app/task-scheduler.jar"]