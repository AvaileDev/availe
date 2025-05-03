# Use the official Gradle image (with JDK) to build the fat JAR
FROM gradle:8.11.1-jdk21

# Set the working directory inside the container
WORKDIR /home/gradle/project

# Copy the Gradle wrapper and wrapper config so we can run ./gradlew
COPY gradlew gradle/ ./
RUN chmod +x ./gradlew

# Copy the rest of the project files
COPY . .

# Build the fat JAR (produces server-all.jar in server/build/libs)
RUN ./gradlew :server:shadowJar --no-daemon --stacktrace

# Documentation for which port it runs on
EXPOSE 8080

# When the container starts, run the fat JAR
CMD ["java", "-jar", "server/build/libs/server-all.jar"]
