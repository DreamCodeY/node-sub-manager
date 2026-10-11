# ---- 构建 ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

# ---- 运行 ----
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/node-sub-manager-0.1.0.jar app.jar
VOLUME /app/data
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
