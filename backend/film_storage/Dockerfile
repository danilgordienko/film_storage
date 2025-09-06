# === Этап сборки ===
FROM maven:3.9.8-eclipse-temurin-21 AS build

# Устанавливаем рабочую директорию
WORKDIR /app

# Сначала копируем только pom.xml и скачиваем зависимости (кэширование)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Теперь копируем исходники и собираем
COPY src ./src
RUN mvn clean package -Dmaven.test.skip=true

# === Этап запуска ===
FROM eclipse-temurin:21-jdk-jammy

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем jar из предыдущего этапа
COPY --from=build /app/target/*.jar app.jar

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "app.jar"]
