# Use Amazon Corretto Alpine3.20 as the base image
FROM amazoncorretto:23.0.1-alpine3.20

# Set the working directory
WORKDIR /strategiclear

# Copy the application jar file into the container
COPY target/strategiclear-0.0.1-SNAPSHOT.jar strategiclear-0.0.1-SNAPSHOT.jar

# Expose the application port (replace 8888 with your app's port if different)
EXPOSE 8888

# Run the application
ENTRYPOINT ["java", "-jar", "strategiclear-0.0.1-SNAPSHOT.jar"]
