FROM eclipse-temurin:25-jdk AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B
COPY src ./src
RUN ./mvnw package -DskipTests -B

FROM eclipse-temurin:25-jre AS runtime
WORKDIR /app
RUN addgroup --system segueme && adduser --system --ingroup segueme segueme
COPY --from=build /app/target/segueme-*.jar app.jar
USER segueme
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
