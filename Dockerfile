# --- 1) Angular bauen ---
FROM node:22-alpine AS fe
WORKDIR /fe
COPY frontend/package.json ./
RUN npm install
COPY frontend/ ./
RUN npx ng build

# --- 2) Spring Boot bauen (Frontend wird als statische Ressource eingebettet) ---
FROM maven:3.9-eclipse-temurin-21 AS be
WORKDIR /be
COPY backend/pom.xml ./
RUN mvn -q dependency:go-offline
COPY backend/src ./src
COPY --from=fe /fe/dist/unchicks/browser ./src/main/resources/static
RUN mvn -q package -DskipTests

# --- 3) Runtime ---
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=be /be/target/unchicks.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
