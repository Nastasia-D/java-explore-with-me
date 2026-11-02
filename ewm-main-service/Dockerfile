FROM eclipse-temurin:21-jre

WORKDIR /app

COPY main-server/target/main-server-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
