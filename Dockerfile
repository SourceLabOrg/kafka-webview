# Set the base image
FROM openjdk:8-jre-alpine

# Dockerfile author / maintainer
MAINTAINER SourceLab.org <stephen.powis@gmail.com>

# Create app and data directories
RUN mkdir -p /app
WORKDIR /app

# Download latest distribution inside image
RUN wget https://github.com/SourceLabOrg/kafka-webview/releases/download/v1.0.0/kafka-webview-ui-1.0.0-bin.zip

# Extract package into /app stripping top level directory contained within the zip.
RUN unzip -d /app kafka-webview-ui-*-bin.zip && rm -f /app/kafka-webview-ui-*-bin.zip && f=`ls` && mv /app/*/* /app && rmdir $f

# Create volume to persist data
VOLUME /app/data

# Expose port
EXPOSE 8080

# What to run when the container starts
ENTRYPOINT [ "/app/start.sh" ]

