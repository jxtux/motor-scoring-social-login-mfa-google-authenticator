FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
RUN mvn -B -ntp clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
COPY --from=build /workspace/motor-scoring-bootstrap/target/motor-scoring-app.jar /app/app.jar
EXPOSE 8443
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
