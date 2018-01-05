#!/bin/bash

CWD=`pwd`

## Change to local directory
cd "${0%/*}"

## Define configuration
export SPRING_CONFIG_LOCATION=classpath:/config/base.yml,config.yml

## launch webapp
java -jar kafka-webview-ui-*.jar

## Change back to previous directory
cd $CWD