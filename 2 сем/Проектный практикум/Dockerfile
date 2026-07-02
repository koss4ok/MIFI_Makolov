FROM maven:3.9.9-eclipse-temurin-17-alpine AS build

WORKDIR /app
COPY pom.xml ./
COPY src ./src

RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app
COPY --from=build /app/target/*-shaded.jar /app/app.jar


EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
