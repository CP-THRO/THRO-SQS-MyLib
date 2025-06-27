# BUILD STAGE
FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /app

# Copy source
COPY . .

# Build Spring Boot fat jar
RUN ./gradlew bootJar --no-daemon

# Runtime
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy the jar from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose port 8080
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "/app/app.jar"]