FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app

# Установка Maven
RUN apt-get update && \
    apt-get install -y wget && \
    wget https://downloads.apache.org/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.tar.gz  && \
    mkdir -p /opt/maven && \
    tar -xzf apache-maven-3.9.9-bin.tar.gz -C /opt/maven && \
    ln -s /opt/maven/apache-maven-3.9.9 /opt/maven/mvn && \
    rm apache-maven-3.9.9-bin.tar.gz

ENV MAVEN_HOME=/opt/maven/mvn
ENV PATH="${MAVEN_HOME}/bin:${PATH}"

# Копируем проект
COPY pom.xml .
COPY src ./src

# Сборка с пропуском тестов
RUN mvn package -DskipTests

# Финальный образ
FROM eclipse-temurin:17.0.3_7-jre
WORKDIR /app
COPY --from=builder /app/target/demo-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]