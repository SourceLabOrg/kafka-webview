#!/bin/bash

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

## For JVM >= 16 add exports.
if [[ -z "$EXPORT_OPTS" ]]; then
    export JAVA_VER=`java -version 2>&1 | head -1 | cut -d'"' -f2 | sed '/^1\./s///' | cut -d'.' -f1`
    if [[ $JAVA_VER -ge 16 ]]; then
      export EXPORT_OPTS="--add-exports=java.naming/com.sun.jndi.ldap=ALL-UNNAMED"
    fi
fi

## Define configuration
export SPRING_CONFIG_LOCATION=classpath:/config/base.yml,config.yml

## launch webapp
exec java $EXPORT_OPTS -jar kafka-webview-ui-*.jar $HEAP_OPTS $LOG_OPTS

## Change back to previous directory
cd $CWD
