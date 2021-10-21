FROM maven:3.8-adoptopenjdk-15 as builder
WORKDIR /app
COPY ./pom.xml ./pom.xml
RUN mvn dependency:go-offline -B
COPY ./src ./src
RUN mvn package && cp target/*.jar application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM cockroachdb/cockroach:latest as cockroach

FROM adoptopenjdk:15-jdk
COPY --from=builder /app/dependencies/ ./
COPY --from=builder /app/snapshot-dependencies/ ./
COPY --from=builder /app/spring-boot-loader/ ./
COPY --from=builder /app/application/ ./
COPY --from=cockroach /cockroach/ ./

RUN mkdir -pv ./.cockroach-certs
RUN mkdir -pv ./.cockroach-key

EXPOSE 9999

ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]