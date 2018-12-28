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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sourcelab.kafka.webview.ui.manager.kafka.KafkaClientConfigUtil;
import org.sourcelab.kafka.webview.ui.manager.kafka.producer.config.WebProducerConfig;

import java.util.Map;
import java.util.Properties;

/**
 * Creates KafkaProducer instances.
 */
public class KafkaProducerFactory {
    private final static Logger logger = LoggerFactory.getLogger(KafkaProducerFactory.class);

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

    public WebKafkaProducer createWebProducer(final WebProducerConfig producerConfig) {
        final Map<String, Object> producerProperties = buildProducerProperties(producerConfig);
        final KafkaProducer kafkaProducer = createProducer(producerProperties);

        return new WebKafkaProducer(kafkaProducer, producerConfig);
    }

    private KafkaProducer createProducer(final Map<String, Object> producerProperties) {
        return new KafkaProducer(producerProperties);
    }

    private Map<String, Object> buildProducerProperties(final WebProducerConfig producerConfig) {
        final Map<String, Object> producerProperties = configUtil.applyCommonSettings(
            producerConfig.getClusterConfig(),
            producerConfig.getProducerClientId()
        );
        producerProperties.put("acks", "all");
        producerProperties.put("max.in.flight.requests.per.connection", 1);
        producerProperties.put("retries", 5);
        producerProperties.put("batch.size", 0);
        producerProperties.put("key.serializer", producerConfig.getKeyTransformer().getSerializerClass());
        producerProperties.put("value.serializer", producerConfig.getValueTransformer().getSerializerClass());

        return producerProperties;
    }
}
