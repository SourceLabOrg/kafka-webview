#!/bin/bash

## These are the required ENV variables for startup for development
## Define what configs to load on start
export SPRING_CONFIG_LOCATION=classpath:/config/base.yml,classpath:/config/dev.yml

## What port to listen on
export PORT=8090

## run application via maven
mvn -pl kafka-webview-ui spring-boot:run -DskipCheckStyle=true -DskipTests=true -DskipLicenseCheck=true