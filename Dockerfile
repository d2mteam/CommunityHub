FROM gradle:8.7-jdk21 AS build
WORKDIR /home/gradle/src
COPY build.gradle settings.gradle gradlew gradlew.bat /home/gradle/src/
COPY gradle /home/gradle/src/gradle
RUN ./gradlew --no-daemon dependencies
COPY src /home/gradle/src/src
RUN ./gradlew --no-daemon bootJar

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /home/gradle/src/build/libs/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
