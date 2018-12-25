#!/bin/bash

set -e

TRUST_STORE_FILE="src/main/resources/kafka.truststore.jks"
KEY_STORE_FILE="src/main/resources/kafka.keystore.jks"
JAR_FILE="target/dev-cluster-2.1.0.jar"
JAAS_FILE="src/main/resources/jaas.conf"

## Change to local directory
cd "${0%/*}"

## Ensure certificates exist
if [ ! -f $KEY_STORE_FILE ] || [ ! -f $TRUST_STORE_FILE ]; then
    echo "Key store files do no exist...generating now...";
    ## Build certificates
    ./../../generateDummySslCertificates.sh
fi

## If jar doesn't exit, build it.
if [ ! -f $JAR_FILE ]; then
    echo "Building package...";
    mvn clean package -DskipTests=true -DskipLicenseCheck=true
fi

## Execute.
echo "starting..."
java -Djava.security.auth.login.config=$JAAS_FILE -cp "${JAR_FILE}:target/dependency/*" org.sourcelab.kafka.devcluster.DevCluster "$@"
