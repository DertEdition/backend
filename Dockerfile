FROM eclipse-temurin:17
WORKDIR /app
COPY app-0.0.1-SNAPSHOT.jar app.jar
CMD ["java","-jar","app.jar"]