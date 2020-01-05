
# Base image
FROM openjdk:11-stretch

# Labels
LABEL maintainer="SourceLab.org <stephen.powis@gmail.com>"

# Arguments
## Ports
ARG UI_PORT=8080
ARG MNG_PORT=9090
## Release configs
ARG WEBVIEW_VER="2.5.0"
ARG WEBVIEW_SHA1="00a8db474ba2c584c5a473c8ca9acbd0259c01de"
ARG WEBVIEW_URL="https://oss.sonatype.org/service/local/repositories/orgsourcelab-1031/content/org/sourcelab/kafka-webview-ui/$WEBVIEW_VER/kafka-webview-ui-$WEBVIEW_VER-bin.zip"

# Set working directory
WORKDIR "/app"

# Get release
RUN curl -fSL -o /tmp/kafka-webview-ui-bin.zip $WEBVIEW_URL

# Verify SHA1 hash
RUN echo "$WEBVIEW_SHA1 /tmp/kafka-webview-ui-bin.zip" | sha1sum -c -

# Extract, move and clean
RUN unzip -d . /tmp/kafka-webview-ui-bin.zip && \
    mv ./kafka-webview-ui-$WEBVIEW_VER/* . && \
    rm -rf ./kafka-webview-ui-$WEBVIEW_VER/ && \
    rm -rf ./src && \
    rm -f /tmp/kafka-webview-ui-bin.zip

# Expose ports
EXPOSE $UI_PORT $MNG_PORT

# Change owner of application main folder
RUN chown -hR 1001:1001 /app/

# Configure a user different than root
USER 1001

# Run application
ENTRYPOINT [ "./start.sh" ]
