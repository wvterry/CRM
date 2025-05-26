# === Стадия 1: builder (используем официальный образ Maven) ===
FROM maven:3.9.9-amazoncorretto-17-debian AS builder
WORKDIR /app

# Копируем проект
COPY pom.xml .
COPY src ./src

# Собираем проект с пропуском тестов
RUN mvn clean package -DskipTests

# === Стадия 2: финальный образ (минималистичный с JRE) ===
FROM eclipse-temurin:17.0.3_7-jre
WORKDIR /app

# Копируем готовый JAR из стадии builder
COPY --from=builder /app/target/demo-0.0.1-SNAPSHOT.jar app.jar

# Точка входа
ENTRYPOINT ["java", "-jar", "app.jar"]