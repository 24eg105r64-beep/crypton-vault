# ---------- Stage 1: build the app ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Download dependencies first (cached between builds)
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy the source code and build the jar
COPY src src
RUN ./mvnw package -DskipTests -B

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
