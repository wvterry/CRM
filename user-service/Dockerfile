FROM maven:3.9.9-amazoncorretto-17-debian AS builder

WORKDIR /app

# Копируем только pom.xml
COPY pom.xml .
COPY crm/pom.xml ./crm/
COPY user-service/pom.xml ./user-service/

RUN mvn dependency:go-offline -B

# Копируем весь код
COPY . .

# Собираем только user-service
RUN mvn package -pl user-service -am -DskipTests

FROM eclipse-temurin:17.0.3_7-jre
WORKDIR /app
COPY --from=builder /app/user-service/target/user-service-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
