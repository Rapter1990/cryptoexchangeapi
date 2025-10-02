# ---------- Stage 1: Build ----------
FROM maven:3.9.11-amazoncorretto-25 AS build

# Work dir
WORKDIR /build

# Copy Maven descriptor + wrapper dir first to leverage layer caching
COPY pom.xml .
COPY .mvn .mvn

# Copy sources and build
COPY src src
RUN mvn -q clean package -DskipTests

# ---------- Stage 2: Runtime ----------
FROM amazoncorretto:25

# Work dir
WORKDIR /app

# Copy the built JAR
COPY --from=build /build/target/*.jar cryptoexchangeapi.jar

# Service port
EXPOSE 1927

# Run with preview features enabled (Java 25)
ENTRYPOINT ["java", "--enable-preview", "-jar", "cryptoexchangeapi.jar"]