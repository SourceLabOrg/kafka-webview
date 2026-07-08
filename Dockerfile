# ---- Stage 1: build from source ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

COPY . .

# Build only the webapp module (and the plugin module it depends on).
RUN mvn -pl kafka-webview-ui -am clean package -DskipTests -B

# ---- Stage 2: runtime ----
FROM eclipse-temurin:21-jre

# Load the packaged base config plus an overridable external config file.
ENV SPRING_CONFIG_LOCATION=classpath:/config/base.yml,/app/config.yml

WORKDIR /app

COPY --from=build /build/kafka-webview-ui/target/kafka-webview-ui-*.jar /app/kafka-webview-ui.jar
COPY --from=build /build/kafka-webview-ui/src/assembly/distribution/config.yml /app/config.yml

# H2 database and uploaded plugin/keystore files live under /app/data.
RUN mkdir -p /app/data/uploads /app/logs
VOLUME /app/data

# 8080 = web ui, 9090 = actuator
EXPOSE 8080 9090

ENTRYPOINT ["java", "-jar", "/app/kafka-webview-ui.jar"]
