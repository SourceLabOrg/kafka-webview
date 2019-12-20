#!/bin/sh

# Temporary save current directory
CWD=`pwd`

# Change to local directory
cd "${0%/*}"

# Start application
export JVM_OPTS="-noverify -server -XX:TieredStopAtLevel=1"
#export MEM_OPTS="-Xms2G -Xmx2G -XX:MaxMetaspaceSize=300M"
exec java $JVM_OPTS $MEM_OPTS $JAVA_OPTS -jar kafka-webview-ui-*.jar

# Change back to previous directory
cd $CWD
