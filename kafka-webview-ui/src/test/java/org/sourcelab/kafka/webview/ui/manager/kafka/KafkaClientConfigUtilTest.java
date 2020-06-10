/**
 * MIT License
 *
 * Copyright (c) 2017, 2018, 2019 SourceLab.org (https://github.com/SourceLabOrg/kafka-webview/)
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

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.junit.Test;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.ClusterConfig;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class KafkaClientConfigUtilTest {
    private final KafkaClientConfigUtil util = new KafkaClientConfigUtil("/tmp", "TestPrefix");

    private final String consumerId = "MyConsumer";
    private final String expectedFinalGroupId = "TestPrefix-MyConsumer";
    private final String expectedFinalConsumerId = "TestPrefix-MyConsumer-" + Thread.currentThread().getId();
    private final String expectedBrokerHosts = "localhost:9092,yourHost:8282";
    private final int expectedRequestTimeoutValue = 15000;

    private final String expectedKeyStoreFile = "myKey.file";
    private final String expectedKeyStorePassword = "keyPass";
    private final String expectedTrustStoreFile = "myKey.file";
    private final String expectedTrustStorePassword = "keyPass";

    private final String expectedSaslPlainMechanism = "PLAIN";
    private final String expectedSaslUsername = "my-sasl-user";
    private final String expectedSaslPassword = "my-sasl-password";

    private final String expectedSaslCustomMechanism = "Custom";
    private final String expectedSaslJaas = "Custom Jaas stuff";

    /**
     * Basic smoke test, without SSL or SASL options.
     */
    @Test
    public void testApplyCommonSettings_noSsl_noSasl() {
        final ClusterConfig clusterConfig = ClusterConfig.newBuilder()
            .withBrokerHosts(expectedBrokerHosts)
            .withUseSsl(false)
            .withUseSasl(false)
            .build();

        final Map<String, Object> config = util.applyCommonSettings(clusterConfig, consumerId);

        // Validate
        validateDefaultKeys(config);
        validateNoSsl(config);
        validateNoSasl(config);

        // Validate this is not set.
        validateNoKey(config, CommonClientConfigs.SECURITY_PROTOCOL_CONFIG);
    }

    /**
     * Basic smoke test with SSL and TrustStore, without SASL options.
     */
    @Test
    public void testApplyCommonSettings_withSsl_noSasl_withTrustStore() {
        final ClusterConfig clusterConfig = ClusterConfig.newBuilder()
            .withBrokerHosts(expectedBrokerHosts)
            .withUseSsl(true)
            .withKeyStoreFile(expectedKeyStoreFile)
            .withKeyStorePassword(expectedKeyStorePassword)
            .withTrustStoreFile(expectedTrustStoreFile)
            .withTrustStorePassword(expectedTrustStorePassword)
            .withUseSasl(false)
            .build();

        final Map<String, Object> config = util.applyCommonSettings(clusterConfig, consumerId);

        // Validate
        validateDefaultKeys(config);
        validateSsl(config, "SSL", true, true);
        validateNoSasl(config);
    }

    /**
     * Basic smoke test with SSL and NO TrustStore, without SASL options.
     */
    @Test
    public void testApplyCommonSettings_withSsl_noSasl_withNoTrustStore() {
        final ClusterConfig clusterConfig = ClusterConfig.newBuilder()
            .withBrokerHosts(expectedBrokerHosts)
            .withUseSsl(true)
            .withKeyStoreFile(expectedKeyStoreFile)
            .withKeyStorePassword(expectedKeyStorePassword)
            .withTrustStoreFile(null)
            .withTrustStorePassword(null)
            .withUseSasl(false)
            .build();

        final Map<String, Object> config = util.applyCommonSettings(clusterConfig, consumerId);

        // Validate
        validateDefaultKeys(config);
        validateSsl(config, "SSL", true, false);
        validateNoSasl(config);
    }

    /**
     * Basic smoke test without SSL, with SASL custom mechanism options.
     */
    @Test
    public void testApplyCommonSettings_noSsl_withSasl_customMechanism() {
        final ClusterConfig clusterConfig = ClusterConfig.newBuilder()
            .withBrokerHosts(expectedBrokerHosts)
            .withUseSsl(false)
            .withUseSasl(true)
            .withSaslMechanism(expectedSaslCustomMechanism)
            .withSaslJaas(expectedSaslJaas)
            .build();

        final Map<String, Object> config = util.applyCommonSettings(clusterConfig, consumerId);

        // Validate
        validateDefaultKeys(config);
        validateNoSsl(config);
        validateSaslCustomMechanism(config);
    }

    /**
     * Basic smoke test without SSL, with SASL plain mechanism options.
     */
    @Test
    public void testApplyCommonSettings_noSsl_withSasl_plainMechanism() {
        final ClusterConfig clusterConfig = ClusterConfig.newBuilder()
            .withBrokerHosts(expectedBrokerHosts)
            .withUseSsl(false)
            .withUseSasl(true)
            .withSaslMechanism(expectedSaslPlainMechanism)
            .withSaslPlaintextUsername(expectedSaslUsername)
            .withSaslPlaintextPassword(expectedSaslPassword)
            .withSaslJaas(expectedSaslJaas)
            .build();

        final Map<String, Object> config = util.applyCommonSettings(clusterConfig, consumerId);

        // Validate
        validateDefaultKeys(config);
        validateNoSsl(config);
        validateSaslPlainMechanism(config, "SASL_PLAINTEXT");
    }

    /**
     * Basic smoke test with SSL and trust store, with SASL plain mechanism options.
     */
    @Test
    public void testApplyCommonSettings_withSsl_withSasl_withTrustStore() {
        final ClusterConfig clusterConfig = ClusterConfig.newBuilder()
            .withBrokerHosts(expectedBrokerHosts)
            .withUseSsl(true)
            .withKeyStoreFile(expectedKeyStoreFile)
            .withKeyStorePassword(expectedKeyStorePassword)
            .withTrustStoreFile(expectedTrustStoreFile)
            .withTrustStorePassword(expectedTrustStorePassword)
            .withUseSasl(true)
            .withSaslMechanism(expectedSaslPlainMechanism)
            .withSaslPlaintextUsername(expectedSaslUsername)
            .withSaslPlaintextPassword(expectedSaslPassword)
            .withSaslJaas(expectedSaslJaas)
            .build();

        final Map<String, Object> config = util.applyCommonSettings(clusterConfig, consumerId);

        // Validate
        validateDefaultKeys(config);
        validateSsl(config, "SASL_SSL", false, true);
        validateSaslPlainMechanism(config, "SASL_SSL");
    }

    /**
     * Basic smoke test with SSL and NO truststore, with SASL plain mechanism options.
     */
    @Test
    public void testApplyCommonSettings_withSsl_withSasl_withNoTrustStore() {
        final ClusterConfig clusterConfig = ClusterConfig.newBuilder()
            .withBrokerHosts(expectedBrokerHosts)
            .withUseSsl(true)
            .withKeyStoreFile(expectedKeyStoreFile)
            .withKeyStorePassword(expectedKeyStorePassword)
            .withTrustStoreFile(null)
            .withTrustStorePassword(null)
            .withUseSasl(true)
            .withSaslMechanism(expectedSaslPlainMechanism)
            .withSaslPlaintextUsername(expectedSaslUsername)
            .withSaslPlaintextPassword(expectedSaslPassword)
            .withSaslJaas(expectedSaslJaas)
            .build();

        final Map<String, Object> config = util.applyCommonSettings(clusterConfig, consumerId);

        // Validate
        validateDefaultKeys(config);
        validateSsl(config, "SASL_SSL", false, false);
        validateSaslPlainMechanism(config, "SASL_SSL");
    }

    private void validateSsl(
        final Map<String, Object> config,
        final String expectedSecurityProtocol,
        final boolean shouldHaveKeyStoreConfiguration,
        final boolean shouldHaveTrustStoreConfiguration
    ) {
        assertNotNull(config);
        validateKey(config, CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, expectedSecurityProtocol);
        if (shouldHaveTrustStoreConfiguration) {
            validateKey(config, SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, "/tmp/" + expectedTrustStoreFile);
            validateKey(config, SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, expectedTrustStorePassword);
        } else {
            validateNoKey(config, SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG);
            validateNoKey(config, SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG);
        }

        if (shouldHaveKeyStoreConfiguration) {
            validateKey(config, SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, "/tmp/" + expectedKeyStoreFile);
            validateKey(config, SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, expectedKeyStorePassword);
        } else {
            validateNoKey(config, SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG);
            validateNoKey(config, SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG);
        }
    }

    private void validateNoSsl(final Map<String, Object> config) {
        assertNotNull(config);
        validateNoKey(config, SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG);
        validateNoKey(config, SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG);
        validateNoKey(config, SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG);
        validateNoKey(config, SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG);
    }

    private void validateSaslCustomMechanism(final Map<String, Object> config) {
        assertNotNull(config);
        validateKey(config, CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
        validateKey(config, "sasl.mechanism", expectedSaslCustomMechanism);
        validateKey(config, "sasl.jaas.config", expectedSaslJaas);
    }

    private void validateSaslPlainMechanism(final Map<String, Object> config, final String expectedSecurityProtocol) {
        assertNotNull(config);
        validateKey(config, CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, expectedSecurityProtocol);
        validateKey(config, "sasl.mechanism", expectedSaslPlainMechanism);
        validateKey(config, "sasl.jaas.config", expectedSaslJaas);
    }

    private void validateNoSasl(final Map<String, Object> config) {
        assertNotNull(config);
        validateNoKey(config, "sasl.mechanism");
        validateNoKey(config, "sasl.jaas.config");
    }

    private void validateDefaultKeys(final Map<String, Object> config) {
        assertNotNull(config);
        validateKey(config, AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, expectedBrokerHosts);
        validateKey(config, AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, expectedRequestTimeoutValue);
        validateKey(config, AdminClientConfig.CLIENT_ID_CONFIG, expectedFinalConsumerId);
        validateKey(config, ConsumerConfig.GROUP_ID_CONFIG, expectedFinalGroupId);
    }

    private void validateKey(final Map<String, Object> config, final String key, final Object expectedValue) {
        assertTrue("Should contain key " + key, config.containsKey(key));
        assertEquals("Invalid Value for key " + key, expectedValue, config.get(key));
    }

    private void validateNoKey(final Map<String, Object> config, final String key) {
        assertFalse("Should not contain key " + key, config.containsKey(key));
    }
}