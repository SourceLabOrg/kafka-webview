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

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.sourcelab.kafka.webview.ui.manager.encryption.SecretManager;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.ClusterConfig;
import org.sourcelab.kafka.webview.ui.model.Cluster;

import java.util.Map;

/**
 * Factory for creating an AdminClient and wrapping it with KafkaOperations.
 */
public class KafkaOperationsFactory {
    /**
     * Defines the consumerId.
     */
    private static final String consumerIdPrefix = "KafkaWebView-Operation-UserId";

    private final SecretManager secretManager;
    private final KafkaAdminFactory kafkaAdminFactory;

    /**
     * Constructor.
     */
    public KafkaOperationsFactory(final SecretManager secretManager, final KafkaAdminFactory kafkaAdminFactory) {
        this.secretManager = secretManager;
        this.kafkaAdminFactory = kafkaAdminFactory;
    }

    /**
     * Factory method.
     * @param cluster What cluster to connect to.
     * @param userId What userId to associate the connection with.
     * @return KafkaOperations client.
     */
    public KafkaOperations create(final Cluster cluster, final long userId) {
        final String clientId = getClientId(userId);

        // Create new Operational Client
        final ClusterConfig clusterConfig = ClusterConfig.newBuilder(cluster, secretManager).build();
        final AdminClient adminClient = kafkaAdminFactory.create(clusterConfig, clientId);
        final KafkaConsumer<String, String> kafkaConsumer = kafkaAdminFactory.createConsumer(clusterConfig, clientId);

        return new KafkaOperations(adminClient, kafkaConsumer);
    }

    /**
     * Build the configuration for the underlying consumer client.
     * @param cluster What cluster to connect to.
     * @param userId What userId to associate the connection with.
     * @return Map of kafka client properties.
     */
    public Map<String, Object> getConsumerConfig(final Cluster cluster, final long userId) {
        final String clientId = getClientId(userId);
        final ClusterConfig clusterConfig = ClusterConfig.newBuilder(cluster, secretManager).build();
        return kafkaAdminFactory.getConsumerConfig(clusterConfig, clientId);
    }

    private String getClientId(final long userId) {
        return consumerIdPrefix + userId;
    }
}
