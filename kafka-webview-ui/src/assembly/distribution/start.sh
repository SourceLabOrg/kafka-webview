#!/bin/sh

# Temporary save current directory
CWD=`pwd`

# Change to local directory
cd "${0%/*}"

# Define environment variables
## New
export JVM_OPTS="-noverify -server -XX:TieredStopAtLevel=1"
export MEM_OPTS="-Xms2G -Xmx2G -XX:MaxMetaspaceSize=300M"
export JAVA_OPTS=""
## Deprecated
if [[ ! -z "$LOG_OPTS" ]]; then
    echo "Usage of 'LOG_OPTS' is deprecated and will be removed in the future, please switch to 'JAVA_OPTS'"
    JAVA_OPTS="$JAVA_OPTS $LOG_OPTS"
fi
if [[ ! -z "$HEAP_OPTS" ]]; then
    echo "Usage of 'HEAP_OPTS' is deprecated and will be removed in the future, please switch to 'MEM_OPTS'"
    export MEM_OPTS="$HEAP_OPTS"
fi

# Start application
exec java $JVM_OPTS $MEM_OPTS $JAVA_OPTS -jar kafka-webview-ui-*.jar

# Change back to previous directory
cd $CWD
