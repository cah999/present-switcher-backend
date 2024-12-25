# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the Gradle wrapper and build files
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Copy the source code
COPY src src

# Make the Gradle wrapper executable
RUN chmod +x gradlew

# Build the application
RUN ./gradlew bootJar

# Expose the port the application runs on
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "build/libs/present-switcher-backend-0.0.1-SNAPSHOT.jar"]