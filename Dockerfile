FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/identity-service.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
