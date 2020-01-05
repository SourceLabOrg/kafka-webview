
# Base image
FROM openjdk:11-stretch

# Labels
LABEL maintainer="SourceLab.org <stephen.powis@gmail.com>"

# Arguments
## Ports
ARG UI_PORT=8080
ARG MNG_PORT=9090
## Java configs
ARG JVM_OPTS="-noverify -server -XX:TieredStopAtLevel=1"
ARG MEM_OPTS="-Xms2G -Xmx2G -XX:MaxMetaspaceSize=300M"
ARG JAVA_OPTS=""

# Set working directory
WORKDIR "/app"

# Get release
COPY kafka-webview-ui/target/*.jar kafka-webview.jar

# Expose ports
EXPOSE $UI_PORT $MNG_PORT

# Change owner of application main folder
RUN chown -hR 1001:1001 /app/

# Configure a user different than root
USER 1001

# Run application
ENTRYPOINT exec java $JVM_OPTS $MEM_OPTS $JAVA_OPTS -jar kafka-webview.jar
