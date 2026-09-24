FROM gradle:8.14-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle clean bootJar -x test --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
