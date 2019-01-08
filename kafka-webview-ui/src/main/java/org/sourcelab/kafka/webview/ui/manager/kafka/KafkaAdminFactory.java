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
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.ClusterConfig;

import java.util.Map;

/**
 * Factory class for Creating new KafkaAdmin instances.
 */
public class KafkaAdminFactory {
    /**
     * Utility class for setting up common kafka client properties.
     */
    private final KafkaClientConfigUtil configUtil;

    /**
     * Constructor.
     */
    public KafkaAdminFactory(final KafkaClientConfigUtil configUtil) {
        if (configUtil == null) {
            throw new RuntimeException("Missing dependency KafkaClientConfigUtil!");
        }
        this.configUtil = configUtil;
    }

    /**
     * Create a new AdminClient instance.
     * @param clusterConfig What cluster to connect to.
     * @param clientId What clientId to associate the connection with.
     * @return AdminClient instance.
     */
    public AdminClient create(final ClusterConfig clusterConfig, final String clientId) {
        // Create a map
        final Map<String, Object> config = configUtil.applyCommonSettings(clusterConfig, clientId);

        // Build admin client.
        return KafkaAdminClient.create(config);
    }

    /**
     * Create a new KafkaConsumer instance.
     * @param clusterConfig What cluster to connect to.
     * @param clientId What clientId to associate the connection with.
     * @return KafkaConsumer instance.
     */
    public KafkaConsumer<String, String> createConsumer(final ClusterConfig clusterConfig, final String clientId) {
        // Create a map
        final Map<String, Object> config = configUtil.applyCommonSettings(clusterConfig, clientId);

        // Set required deserializer classes.
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        // Create consumer
        return new KafkaConsumer<>(config);
    }
}
