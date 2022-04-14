# Set the base image
FROM openjdk:8-jre-alpine

# Dockerfile author / maintainer
MAINTAINER SourceLab.org <stephen.powis@gmail.com>

## Define what version of Kafka Webview to build the image using.
ENV WEBVIEW_VER="2.9.0" \
    WEBVIEW_SHA1="c3ba9db0fb4baaa5dea04a9cfe53dfdea13faf58" \
    WEBVIEW_HOME="/app"

# Create app and data directories
RUN mkdir -p ${WEBVIEW_HOME} && \
    mkdir -p ${WEBVIEW_HOME}/logs && \
    mkdir -p ${WEBVIEW_HOME}/data && \
    apk add --update bash curl && \
    rm -rf /var/cache/apk/*

WORKDIR ${WEBVIEW_HOME}

# Download KafkaWebview Release from Github project
RUN curl -fSL -o /tmp/kafka-webview-ui-bin.zip https://github.com/SourceLabOrg/kafka-webview/releases/download/v${WEBVIEW_VER}/kafka-webview-ui-${WEBVIEW_VER}-bin.zip

# Verify SHA1 hash and extract.
RUN echo "${WEBVIEW_SHA1}  /tmp/kafka-webview-ui-bin.zip" | sha1sum -c - && \
    unzip -d ${WEBVIEW_HOME} /tmp/kafka-webview-ui-bin.zip && \
    ls -l ${WEBVIEW_HOME} && \
    ls -l ${WEBVIEW_HOME}/kafka-webview-ui-${WEBVIEW_VER}/* && \
    mv ${WEBVIEW_HOME}/kafka-webview-ui-${WEBVIEW_VER}/* ${WEBVIEW_HOME} && \
    rm -rf ${WEBVIEW_HOME}/kafka-webview-ui-${WEBVIEW_VER}/ && \
    rm -rf ${WEBVIEW_HOME}/src && \
    rm -f /tmp/kafka-webview-ui-bin.zip

# Create volume to persist data
VOLUME ${WEBVIEW_HOME}/data

# Expose WebUI Port
EXPOSE 8080

# Expose Acuator Port for health checks.
EXPOSE 9090

# What to run when the container starts
ENTRYPOINT [ "/app/start.sh" ]
