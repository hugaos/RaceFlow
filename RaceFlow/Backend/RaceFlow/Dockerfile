# Dockerfile para RaceFlow Backend (Spring Boot)

# Usa uma imagem base do JDK 17
FROM openjdk:17-jdk-slim as builder

# Define o diretório de trabalho
WORKDIR /app

COPY . .

COPY src ./src

RUN ./mvnw package -DskipTests

FROM openjdk:17

# Copia o arquivo JAR gerado pelo Maven para o container
COPY --from=builder app/target/RaceFlow-0.0.1-SNAPSHOT.jar raceflow.jar

# Exposição da porta onde a aplicação Spring Boot irá rodar
EXPOSE 8080

# Comando para iniciar a aplicação Spring Boot
ENTRYPOINT ["java", "-jar", "raceflow.jar"]