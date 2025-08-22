# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.war app.war
COPY src/main/resources/hackathon2025-458305-3dfaf8634507.json /app/credentials.json
ENV GOOGLE_APPLICATION_CREDENTIALS=/app/credentials.json
EXPOSE ${PORT:-8080}
ENTRYPOINT ["java", "-jar", "app.war", "--server.port=${PORT:-8080}", "--server.address=0.0.0.0"] 