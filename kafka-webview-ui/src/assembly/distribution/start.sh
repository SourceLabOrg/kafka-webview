#!/bin/sh

CWD=`pwd`

## Change to local directory
cd "${0%/*}"

# Define empty options as defaults if none set
if [[ -z "$HEAP_OPTS" ]]; then
    export HEAP_OPTS=""
fi
if [[ -z "$LOG_OPTS" ]]; then
    export LOG_OPTS=""
fi

## Define configuration
export SPRING_CONFIG_LOCATION=classpath:/config/base.yml,config.yml

## launch webapp
java -jar kafka-webview-ui-*.jar $HEAP_OPTS $LOG_OPTS

## Change back to previous directory
cd $CWD