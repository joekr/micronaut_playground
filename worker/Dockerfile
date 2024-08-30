# Use an official OpenJDK runtime as a parent image
FROM openjdk:21-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the project's build files (adjust if you're using Gradle or another tool)
COPY build/libs/worker-0.1-all.jar /app/app.jar

# Expose port 8080 for the API service
EXPOSE 8080

# Define the command to run the application
CMD ["java", "-jar", "/app/app.jar"]
