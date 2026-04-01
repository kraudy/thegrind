# ================================================
# Stage 1: Build Angular Frontend
# ================================================
FROM node:20-alpine AS frontend-build

WORKDIR /app/frontend
COPY InventoryFrontend/package*.json ./
RUN npm ci --omit=dev
#RUN npm ci
COPY InventoryFrontend/ ./
RUN npm run build -- --configuration=production
#RUN npm run build

# DEBUG: show what Angular actually produced
RUN echo "=== Angular build output ===" && ls -la /app/frontend/dist/InventoryFrontend

# ================================================
# Stage 2: Build Spring Boot + embed frontend
# ================================================
FROM maven:3.9.9-eclipse-temurin-21 AS backend-build

WORKDIR /app/backend
COPY InventoryBackend/pom.xml InventoryBackend/mvnw ./
COPY InventoryBackend/.mvn .mvn
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B
COPY InventoryBackend/src ./src

# ← Automatically inject Angular build
#COPY --from=frontend-build /app/frontend/dist/InventoryFrontend ./src/main/resources/static

# FIXED & DEBUG: copy contents of Angular dist
# COPY --from=frontend-build /app/frontend/dist/InventoryFrontend/. ./src/main/resources/static/
COPY --from=frontend-build /app/frontend/dist/InventoryFrontend/browser/. ./src/main/resources/static/

# DEBUG: confirm files are in the right place before Maven packages them
RUN echo "=== Files in static before Maven build ===" && ls -la ./src/main/resources/static


RUN ./mvnw clean package -DskipTests

# ================================================
# Stage 3: Final runtime image
# ================================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=backend-build /app/backend/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]