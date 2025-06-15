FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Copy only wrapper files first — these change rarely
COPY gradlew .
COPY gradle/ gradle/

# Make wrapper executable
RUN chmod +x gradlew

# Pre-download dependencies (cached if build.gradle hasn't changed)
COPY build.gradle .
COPY settings.gradle .
RUN ./gradlew dependencies || true

COPY . .

RUN ./gradlew build -x test

FROM eclipse-temurin:21-jre-noble

WORKDIR /app

RUN apt-get update && \
    apt-get install -y ffmpeg && \
    rm -rf /var/lib/apt/lists/*

COPY --from=build /app/build/libs/*.jar app.jar

# Expose the application port
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

