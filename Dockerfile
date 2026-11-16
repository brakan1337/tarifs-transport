# Une seule image : Angular est compilé puis servi par Spring Boot.

# 1. L'interface Angular
FROM node:24-alpine AS interface
WORKDIR /frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npx ng build

# 2. L'API Spring Boot, avec l'interface dans ses fichiers statiques
FROM eclipse-temurin:21-jdk AS api
WORKDIR /backend
COPY backend/.mvn .mvn
COPY backend/mvnw backend/pom.xml ./
RUN chmod +x mvnw && ./mvnw -q -B dependency:go-offline
COPY backend/src src
COPY --from=interface /frontend/dist/frontend/browser src/main/resources/static
RUN ./mvnw -q -B package -DskipTests

# 3. L'image finale : juste Java et le fichier .jar
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 1001 tarifs
COPY --from=api /backend/target/tarifs-transport-*.jar app.jar
USER tarifs
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
