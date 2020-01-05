#!/bin/sh

# Define a profile to load specific configurations
# Available profiles: [null = default = prod] | dev
export SPRING_PROFILES_ACTIVE="dev"

# Define which project should be run (comma separated list)
export PROJ_LIST="kafka-webview-ui"

# Define maven configurations
export MAVEN_CONFIG="-DskipCheckStyle=true -DskipTests=true -DskipLicenseCheck=true"

# Run application using maven
mvn spring-boot:run -pl $PROJ_LIST $MAVEN_CONFIG
