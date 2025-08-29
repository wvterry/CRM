
FROM maven:3.9.9-amazoncorretto-17-debian AS builder

WORKDIR /app

COPY crm/pom.xml ./crm/
COPY pom.xml .
COPY user-service/pom.xml ./user-service/
COPY security-common/pom.xml ./security-common/
COPY . .

RUN mvn dependency:go-offline -B

RUN mvn package -pl crm -am -DskipTests

FROM eclipse-temurin:17.0.3_7-jre
WORKDIR /app
COPY --from=builder /app/crm/target/crm-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]