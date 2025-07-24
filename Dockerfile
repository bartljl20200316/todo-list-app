FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8088

# Run the JAR file
ENTRYPOINT ["java", "-jar", "app.jar"]
