# Stage 1: build the jar and run the tests
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Download dependencies in their own layer, so they are cached until pom.xml changes
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline

COPY src/ src/
RUN ./mvnw package

# Stage 2: run the jar with only the JRE
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
