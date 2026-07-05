FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline -B

COPY . .

RUN mvn package -DskipTests

FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar
COPY --from=build /app/data ./data

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]