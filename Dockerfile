FROM eclipse-temurin:17-jdk

RUN apt-get update && apt-get install -y maven

WORKDIR /app

# Copy pom.xml first for dependency caching
COPY pom.xml /app/pom.xml

# Clean and install dependencies
RUN mvn clean dependency:resolve -U || true

# Create src directory structure so volume mount works
RUN mkdir -p /app/src

# Source code will be mounted as volume
# This will be overridden by docker-compose volume mount

# Run with Maven Spring Boot plugin for hot reload
CMD ["mvn", "spring-boot:run"]