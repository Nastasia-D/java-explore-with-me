FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

ARG JAR_FILE=stats-server/target/*.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app/app.jar"]
