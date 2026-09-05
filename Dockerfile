# --- Stage 1 : build ---
FROM eclipse-temurin:26-jdk AS build
WORKDIR /app

# Copie du wrapper Gradle et des fichiers de config en premier (cache Docker)
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew

# Pré-téléchargement des dépendances (optionnel mais accélère les rebuilds)
RUN ./gradlew dependencies --no-daemon || true

# Copie du code source et build du jar
COPY src src
RUN ./gradlew bootJar -x test --no-daemon

# --- Stage 2 : runtime ---
FROM eclipse-temurin:26-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]