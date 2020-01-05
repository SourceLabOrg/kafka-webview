#!/bin/sh

# Get the directory of this script
SCRIPT_DIR=$(dirname "$0")

# Define environment variables
## New
if [[ -z "$JVM_OPTS" ]]; then
    export JVM_OPTS="-noverify -server -XX:TieredStopAtLevel=1"
fi
if [[ -z "$MEM_OPTS" ]]; then
    export MEM_OPTS="-Xms2G -Xmx2G -XX:MaxMetaspaceSize=300M"
fi
if [[ -z "$JAVA_OPTS" ]]; then
    export JAVA_OPTS=""
fi
## Deprecated
if [[ ! -z "$LOG_OPTS" ]]; then
    echo "Usage of 'LOG_OPTS' is deprecated and will be removed in the future, please switch to 'JAVA_OPTS'"
    export JAVA_OPTS="$JAVA_OPTS $LOG_OPTS"
fi
if [[ ! -z "$HEAP_OPTS" ]]; then
    echo "Usage of 'HEAP_OPTS' is deprecated and will be removed in the future, please switch to 'MEM_OPTS'"
    export MEM_OPTS="$MEM_OPTS $HEAP_OPTS"
fi

# Start application
echo "Start Kafka-WebView with following parameters:"
echo "\tJVM_OPTS: $JVM_OPTS"
echo "\tMEM_OPTS: $MEM_OPTS"
echo "\tJAVA_OPTS: $JAVA_OPTS"
echo "\tLOG_OPTS: deprecated, added to JAVA_OPTS"
echo "\tHEAP_OPTS: deprecated, added to MEM_OPTS"
echo 
java $JVM_OPTS $MEM_OPTS $JAVA_OPTS -jar kafka-webview-ui-*.jar
