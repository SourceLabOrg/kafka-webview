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

package org.sourcelab.kafka.webview.ui.manager.kafka.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.sourcelab.kafka.webview.ui.manager.kafka.KafkaClientConfigUtil;
import org.sourcelab.kafka.webview.ui.manager.kafka.producer.config.WebProducerConfig;

import java.util.Map;

/**
 * Creates KafkaProducer instances.
 */
public class KafkaProducerFactory {
    /**
     * Utility class for setting up common kafka client properties.
     */
    private final KafkaClientConfigUtil configUtil;

    /**
     * Constructor.
     * @param configUtil Utility class to DRY out common kafka client settings.
     */
    public KafkaProducerFactory(final KafkaClientConfigUtil configUtil) {
        if (configUtil == null) {
            throw new RuntimeException("Missing dependency KafkaClientConfigUtil!");
        }
        this.configUtil = configUtil;
    }

    /**
     * Factory method.
     * @param producerConfig Configuration for the producer.
     * @return new WebKafkaProducer instance.
     */
    public WebKafkaProducer createWebProducer(final WebProducerConfig producerConfig) {
        final Map<String, Object> producerProperties = buildProducerProperties(producerConfig);
        final KafkaProducer kafkaProducer = createProducer(producerProperties);

        return new WebKafkaProducer(kafkaProducer, producerConfig);
    }

    /**
     * Create underlying KafkaProducer instance.
     * @param producerProperties kafka producer configuration properties.
     * @return new KafkaProducer instance.
     */
    private KafkaProducer createProducer(final Map<String, Object> producerProperties) {
        return new KafkaProducer(producerProperties);
    }

    /**
     * Build configuration for KafkaProducer.
     * @param webProducerConfig Configuration set.
     * @return Map of KafkaProducer options.
     */
    private Map<String, Object> buildProducerProperties(final WebProducerConfig webProducerConfig) {
        final Map<String, Object> producerProperties = configUtil.applyCommonSettings(
            webProducerConfig.getClusterConfig(),
            webProducerConfig.getProducerClientId()
        );

        // Default options
        producerProperties.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
        producerProperties.put(ProducerConfig.RETRIES_CONFIG, 5);
        producerProperties.put(ProducerConfig.BATCH_SIZE_CONFIG, 0);

        // Configurable options
        producerProperties.put(ProducerConfig.ACKS_CONFIG, webProducerConfig.getAckRequirement());
        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, webProducerConfig.getKeyTransformer().getSerializerClass());
        producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, webProducerConfig.getValueTransformer().getSerializerClass());
        producerProperties.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, webProducerConfig.getPartitionerClass());

        return producerProperties;
    }
}
