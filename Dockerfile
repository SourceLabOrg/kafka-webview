# Set the base image
FROM openjdk:8

# Dockerfile author / maintainer
MAINTAINER SourceLag.org <stephen.powis@gmail.com>

# This copies binary distribution inside the image
RUN mkdir -p /app
ADD kafka-webview-ui/target/kafka-webview-ui-1.0.0-bin.zip /app

# Extract the distribution
WORKDIR /app
RUN unzip -x kafka-webview-ui-1.0.0-bin.zip

# Expose port
EXPOSE 8080

# What to run when the container starts
ENTRYPOINT [ "/app/kafka-webview-ui-1.0.0/start.sh" ]

