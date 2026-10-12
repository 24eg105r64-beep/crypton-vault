# ---------- Stage 1: build the app (Maven + Java 21 included) ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Download dependencies first (cached between builds)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the source code and build the jar
COPY src src
RUN mvn package -DskipTests -B

# ---------- Stage 2: run the app ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Run as a normal user, not root
RUN useradd -r -u 1001 crypton
USER crypton

# Copy only the built jar from stage 1
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
