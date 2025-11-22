# Use official OpenJDK image
FROM eclipse-temurin:21-jdk-alpine

# Set working directory
WORKDIR /app

# Copy build output (JAR file)
COPY target/order-service-0.0.1-SNAPSHOT.jar order-service.jar

# Expose port
EXPOSE 8080

# Run the JAR
ENTRYPOINT ["java", "-jar", "order-service.jar"]