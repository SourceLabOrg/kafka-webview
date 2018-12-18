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
import com.salesforce.kafka.test.listeners.SaslPlainListener;
import org.junit.ClassRule;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.ClusterConfig;
import org.sourcelab.kafka.webview.ui.manager.sasl.SaslProperties;
import org.sourcelab.kafka.webview.ui.manager.sasl.SaslUtility;
import org.sourcelab.kafka.webview.ui.model.Cluster;

/**
 * Performs smoke tests against a Kafka Cluster configured with SASL_PLAIN.
 *
 * Test assumes JVM has been launched with the option:
 *   `-Djava.security.auth.login.config=kafka-webview-ui/src/test/resources/jaas.conf`
 */
public class KafkaSaslClusterTests extends AbstractKafkaClusterTests {

    private static final String SASL_USER = "kafkaclient";
    private static final String SASL_PASSWORD = "client-secret";

    /**
     * Setup broker with SASL support.
     */
    @ClassRule
    public static SharedKafkaTestResource sharedKafkaTestResource = new SharedKafkaTestResource()
        .registerListener(new SaslPlainListener()
        .withUsername(SASL_USER)
        .withPassword(SASL_PASSWORD)
    );


    @Override
    public ClusterConfig buildClusterConfig() {
        // Create Cluster config with SASL enabled.
        return ClusterConfig.newBuilder()
            .withBrokerHosts(sharedKafkaTestResource.getKafkaConnectString())
            .withUseSasl(true)
            .withSaslMechanism("PLAIN")
            .withSaslPlaintextUsername(SASL_USER)
            .withSaslPlaintextPassword(SASL_PASSWORD)
            .build();
    }

    @Override
    public KafkaAdminFactory buildKafkaAdminFactory() {
        return new KafkaAdminFactory(
            new KafkaClientConfigUtil("not/used", "MyPrefix")
        );
    }

    @Override
    protected String getExpectedProtocol() {
        return "SASL_PLAINTEXT";
    }

    @Override
    protected KafkaConsumerFactory buildKafkaConsumerFactory() {
        return new KafkaConsumerFactory(
            new KafkaClientConfigUtil("not/used", "TestPrefix")
        );
    }

    @Override
    protected KafkaTestUtils getKafkaTestUtils() {
        return sharedKafkaTestResource.getKafkaTestUtils();
    }

    @Override
    protected Cluster buildCluster() {
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
        cluster.setSslEnabled(false);
        cluster.setSaslEnabled(true);
        cluster.setSaslConfig(saslProperties);
        cluster.setSaslMechanism("PLAIN");
        return cluster;
    }
}
