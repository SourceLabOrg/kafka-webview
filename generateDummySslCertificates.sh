### This script generates a dummy self signed certificate used in the test suite.

#!/bin/bash
cd "$(dirname "$0")"

set -e

KEYTOOL_COMMAND="keytool"
KEYSTORE_FILENAME="kafka.keystore.jks"
VALIDITY_IN_DAYS=3650
DEFAULT_TRUSTSTORE_FILENAME="kafka.truststore.jks"
TRUSTSTORE_WORKING_DIRECTORY="generated/truststore"
KEYSTORE_WORKING_DIRECTORY="generated/keystore"
CA_CERT_FILE="ca-cert"
KEYSTORE_SIGN_REQUEST="cert-file"
KEYSTORE_SIGN_REQUEST_SRL="ca-cert.srl"
KEYSTORE_SIGNED_CERT="cert-signed"
WEBVIEW_UI_DEST_DIRECTORY="kafka-webview-ui/src/test/resources/"
DEV_CLUSTER_DEST_DIRECTORY="dev-cluster/src/main/resources/"

rm -rf generated
mkdir -p $TRUSTSTORE_WORKING_DIRECTORY

openssl req -new -x509 -keyout $TRUSTSTORE_WORKING_DIRECTORY/ca-key -sha256 \
    -out $TRUSTSTORE_WORKING_DIRECTORY/ca-cert -days $VALIDITY_IN_DAYS \
    -subj "/C=JP/ST=Tokyo/L=Japan/O=KafkaWebView/OU=Engineer/CN=localhost" \
    -passout pass:password

trust_store_private_key_file="$TRUSTSTORE_WORKING_DIRECTORY/ca-key"

${KEYTOOL_COMMAND} -keystore $TRUSTSTORE_WORKING_DIRECTORY/$DEFAULT_TRUSTSTORE_FILENAME \
    -alias CARoot -import -file $TRUSTSTORE_WORKING_DIRECTORY/ca-cert \
    -storepass password -trustcacerts -noprompt -J-Dkeystore.pkcs12.legacy
trust_store_file="$TRUSTSTORE_WORKING_DIRECTORY/$DEFAULT_TRUSTSTORE_FILENAME"

rm $TRUSTSTORE_WORKING_DIRECTORY/$CA_CERT_FILE

mkdir $KEYSTORE_WORKING_DIRECTORY
${KEYTOOL_COMMAND} -keystore $KEYSTORE_WORKING_DIRECTORY/$KEYSTORE_FILENAME \
  -alias localhost -validity $VALIDITY_IN_DAYS -genkey -keyalg RSA -sigalg SHA256withRSA\
  -storepass password -keypass password -J-Dkeystore.pkcs12.legacy \
  -dname "CN=localhost, OU=localhost, O=localhost, L=localhost, ST=localhost, C=localhost"

${KEYTOOL_COMMAND} -keystore $trust_store_file -export -alias CARoot -rfc -file $CA_CERT_FILE \
  -storepass password -keypass password -J-Dkeystore.pkcs12.legacy

${KEYTOOL_COMMAND} -keystore $KEYSTORE_WORKING_DIRECTORY/$KEYSTORE_FILENAME -alias localhost \
  -certreq -file $KEYSTORE_SIGN_REQUEST \
  -keypass password -storepass password -J-Dkeystore.pkcs12.legacy

openssl x509 -req -CA $CA_CERT_FILE -CAkey $trust_store_private_key_file -sha256 \
  -in $KEYSTORE_SIGN_REQUEST -out $KEYSTORE_SIGNED_CERT \
  -days $VALIDITY_IN_DAYS -CAcreateserial -passin pass:password

${KEYTOOL_COMMAND} -keystore $KEYSTORE_WORKING_DIRECTORY/$KEYSTORE_FILENAME -alias CARoot \
  -import -file $CA_CERT_FILE \
  -keypass password -storepass password -noprompt -J-Dkeystore.pkcs12.legacy
rm $CA_CERT_FILE

${KEYTOOL_COMMAND} -keystore $KEYSTORE_WORKING_DIRECTORY/$KEYSTORE_FILENAME -alias localhost -import \
  -file $KEYSTORE_SIGNED_CERT -storepass password -keypass password -J-Dkeystore.pkcs12.legacy

rm $KEYSTORE_SIGN_REQUEST_SRL
rm $KEYSTORE_SIGN_REQUEST
rm $KEYSTORE_SIGNED_CERT
rm $trust_store_private_key_file

cp -rp $KEYSTORE_WORKING_DIRECTORY/$KEYSTORE_FILENAME $WEBVIEW_UI_DEST_DIRECTORY
cp -rp $KEYSTORE_WORKING_DIRECTORY/$KEYSTORE_FILENAME $DEV_CLUSTER_DEST_DIRECTORY
cp -rp $trust_store_file $WEBVIEW_UI_DEST_DIRECTORY
cp -rp $trust_store_file $DEV_CLUSTER_DEST_DIRECTORY

rm -rf generated