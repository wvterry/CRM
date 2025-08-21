#FROM maven:3.9.6-eclipse-temurin-17 AS builder
FROM maven:3.9.9-amazoncorretto-17-debian AS builder

WORKDIR /app

COPY pom.xml .
COPY crm/pom.xml ./crm/
COPY user-service/pom.xml ./user-service/
COPY security-common/pom.xml ./security-common/
COPY kafka-admin-service/pom.xml ./kafka-admin-service/
COPY . .

RUN mvn dependency:go-offline -B

#COPY . .

RUN mvn package -pl kafka-admin-service -am -DskipTests

FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=builder /app/kafka-admin-service/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
