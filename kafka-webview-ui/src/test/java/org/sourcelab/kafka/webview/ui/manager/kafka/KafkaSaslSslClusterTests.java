/**
 * MIT License
 *
 * Copyright (c) 2017, 2018 SourceLab.org (https://github.com/Crim/kafka-webview/)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.sourcelab.kafka.webview.ui.manager.kafka;

import com.salesforce.kafka.test.KafkaTestUtils;
import com.salesforce.kafka.test.junit4.SharedKafkaTestResource;
import com.salesforce.kafka.test.listeners.SaslSslListener;
import org.junit.ClassRule;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.ClusterConfig;
import org.sourcelab.kafka.webview.ui.manager.sasl.SaslProperties;
import org.sourcelab.kafka.webview.ui.manager.sasl.SaslUtility;
import org.sourcelab.kafka.webview.ui.model.Cluster;

import java.io.File;

/**
 * Performs smoke tests against a Kafka Cluster configured with SASL_SSL.
 *
 * Test assumes JVM has been launched with the option:
 *   `-Djava.security.auth.login.config=kafka-webview-ui/src/test/resources/jaas.conf`
 */
public class KafkaSaslSslClusterTests extends AbstractKafkaClusterTests {
    private static final String SASL_USER = "kafkaclient";
    private static final String SASL_PASSWORD = "client-secret";

    private final static String KEY_STORE_PASSWORD = "password";
    private final static String TRUST_STORE_PASSWORD = "password";
    private final static String KEY_STORE_FILE = KafkaSaslSslClusterTests.class.getClassLoader()
        .getResource("kafka.keystore.jks")
        .getFile();
    private final static String TRUST_STORE_FILE = KafkaSaslSslClusterTests.class.getClassLoader()
        .getResource("kafka.truststore.jks")
        .getFile();

    /**
     * Setup broker with SASL+SSL support.
     */
    @ClassRule
    public static SharedKafkaTestResource sharedKafkaTestResource = new SharedKafkaTestResource()
        .registerListener(new SaslSslListener()
            // SASL_PLAIN options
            .withUsername(SASL_USER)
            .withPassword(SASL_PASSWORD)
            // SSL Options
            .withKeyStoreLocation(KEY_STORE_FILE)
            .withKeyStorePassword(KEY_STORE_PASSWORD)
            .withTrustStoreLocation(TRUST_STORE_FILE)
            .withTrustStorePassword(TRUST_STORE_PASSWORD)
        );

    @Override
    protected ClusterConfig buildClusterConfig() {
        final File keyStoreFile = new File(KEY_STORE_FILE);
        final String keyStoreFilename = keyStoreFile.getName();
        final String keyStorePath = keyStoreFile.getParent();
        final File trustStoreFile = new File(KEY_STORE_FILE);
        final String trustStoreFilename = trustStoreFile.getName();

        // Create Cluster config with SASL + SSL enabled.
        return ClusterConfig.newBuilder()
            .withBrokerHosts(sharedKafkaTestResource.getKafkaConnectString())
            // SASL Options
            .withUseSasl(true)
            .withSaslMechanism("PLAIN")
            .withSaslPlaintextUsername(SASL_USER)
            .withSaslPlaintextPassword(SASL_PASSWORD)
            // SSL Options
            .withUseSsl(true)
            .withKeyStoreFile(keyStoreFilename)
            .withKeyStorePassword(KEY_STORE_PASSWORD)
            .withTrustStoreFile(trustStoreFilename)
            .withTrustStorePassword(TRUST_STORE_PASSWORD)
            .build();
    }

    @Override
    protected KafkaAdminFactory buildKafkaAdminFactory() {
        return new KafkaAdminFactory(
            buildKafkaClientConfigUtil()
        );
    }

    @Override
    protected String getExpectedProtocol() {
        return "SASL_SSL";
    }

    @Override
    protected KafkaConsumerFactory buildKafkaConsumerFactory() {
        return new KafkaConsumerFactory(buildKafkaClientConfigUtil());
    }

    @Override
    protected KafkaTestUtils getKafkaTestUtils() {
        return sharedKafkaTestResource.getKafkaTestUtils();
    }

    @Override
    protected Cluster buildCluster() {
        final String keyStoreFilename = new File(KEY_STORE_FILE).getName();
        final String trustStoreFilename = new File(KEY_STORE_FILE).getName();

        final SaslUtility saslUtility = new SaslUtility(getSecretManager());
        final String saslProperties = saslUtility.encryptProperties(
            SaslProperties.newBuilder()
                .withPlainUsername(SASL_USER)
                .withPlainPassword(SASL_PASSWORD)
                .withMechanism("PLAIN")
                .build()
        );

        final Cluster cluster = new Cluster();
        cluster.setBrokerHosts(sharedKafkaTestResource.getKafkaConnectString());
        // SASL Options
        cluster.setSaslEnabled(true);
        cluster.setSaslConfig(saslProperties);
        cluster.setSaslMechanism("PLAIN");

        // SSL Options
        cluster.setSslEnabled(true);
        cluster.setKeyStoreFile(keyStoreFilename);
        cluster.setTrustStoreFile(trustStoreFilename);
        // Passwords are stored encrypted.
        cluster.setKeyStorePassword(getSecretManager().encrypt(KEY_STORE_PASSWORD));
        cluster.setTrustStorePassword(getSecretManager().encrypt(TRUST_STORE_PASSWORD));

        return cluster;
    }

    private KafkaClientConfigUtil buildKafkaClientConfigUtil() {
        final File keyStoreFile = new File(KEY_STORE_FILE);
        return new KafkaClientConfigUtil(keyStoreFile.getParent(), "MyPrefix");
    }
}
