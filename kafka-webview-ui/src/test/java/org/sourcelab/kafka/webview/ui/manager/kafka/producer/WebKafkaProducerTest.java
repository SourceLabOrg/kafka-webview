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

package org.sourcelab.kafka.webview.ui.manager.kafka.producer;

import com.salesforce.kafka.test.junit4.SharedKafkaTestResource;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.ClassRule;
import org.junit.Test;
import org.sourcelab.kafka.webview.ui.manager.kafka.KafkaClientConfigUtil;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.ClusterConfig;
import org.sourcelab.kafka.webview.ui.manager.kafka.producer.config.WebProducerConfig;
import org.sourcelab.kafka.webview.ui.manager.kafka.producer.transformer.DefaultTransformer;
import org.sourcelab.kafka.webview.ui.manager.kafka.producer.transformer.LongTransformer;
import org.sourcelab.kafka.webview.ui.manager.kafka.producer.transformer.StringTransformer;


import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class WebKafkaProducerTest {

    @ClassRule
    public static SharedKafkaTestResource sharedKafkaTestResource = new SharedKafkaTestResource();

    private final KafkaClientConfigUtil configUtil = new KafkaClientConfigUtil(
        "n/a",
        "KafkaWebView"
    );

    private final KafkaProducerFactory factory = new KafkaProducerFactory(configUtil);

    /**
     * Test publishing string key/value into kafka.
     */
    @Test
    public void testStringSerialization() {
        final String testKeyString = "This is my key string!";
        final String testValueString = "This is my value string!";

        // Create a test topic
        final String topic = "TestProducerTopic-" + System.currentTimeMillis();
        sharedKafkaTestResource.getKafkaTestUtils()
            .createTopic(topic, 1, (short) 1);

        // Use StringTransformer
        final DefaultTransformer<String> transformer = new StringTransformer();

        // Defines the Cluster
        final ClusterConfig clusterConfig = ClusterConfig.newBuilder()
            .withBrokerHosts(sharedKafkaTestResource.getKafkaConnectString())
            .build();

        // Create a config
        final WebProducerConfig config = WebProducerConfig.newBuilder()
            .withKeyTransformer(transformer)
            .withValueTransformer(transformer)
            .withProducerClientId("MyClientId")
            .withClusterConfig(clusterConfig)
            .withTopic(topic)
            .build();

        final WebKafkaProducer kafkaProducer = factory.createWebProducer(config);
        assertNotNull(kafkaProducer);

        final Map<String, String> keyValues = DefaultTransformer.createDefaultValueMap(testKeyString);
        final Map<String, String> valueValues = DefaultTransformer.createDefaultValueMap(testValueString);


        // Create record to publish
        final WebProducerRecord record = new WebProducerRecord(keyValues, valueValues);

        // Publish
        kafkaProducer.produce(record);

        // Validate the record now exists in kafka
        validatePublishedRecord(topic, StringDeserializer.class, StringDeserializer.class, testKeyString, testValueString);
    }

    /**
     * Test publishing long key/value into kafka.
     */
    @Test
    public void testLongSerialization() {
        final Long testKeyString = 123L;
        final Long testValueString = 321L;

        // Create a test topic
        final String topic = "TestProducerTopic-" + System.currentTimeMillis();
        sharedKafkaTestResource.getKafkaTestUtils()
            .createTopic(topic, 1, (short) 1);

        // Use StringTransformer
        final DefaultTransformer<Long> transformer = new LongTransformer();

        // Defines the Cluster
        final ClusterConfig clusterConfig = ClusterConfig.newBuilder()
            .withBrokerHosts(sharedKafkaTestResource.getKafkaConnectString())
            .build();

        // Create a config
        final WebProducerConfig config = WebProducerConfig.newBuilder()
            .withKeyTransformer(transformer)
            .withValueTransformer(transformer)
            .withProducerClientId("MyClientId")
            .withClusterConfig(clusterConfig)
            .withTopic(topic)
            .build();

        final WebKafkaProducer kafkaProducer = factory.createWebProducer(config);
        assertNotNull(kafkaProducer);

        final Map<String, String> keyValues = DefaultTransformer.createDefaultValueMap(testKeyString.toString());
        final Map<String, String> valueValues = DefaultTransformer.createDefaultValueMap(testValueString.toString());


        // Create record to publish
        final WebProducerRecord record = new WebProducerRecord(keyValues, valueValues);

        // Publish
        kafkaProducer.produce(record);

        // Validate the record now exists in kafka
        validatePublishedRecord(topic, LongDeserializer.class, LongDeserializer.class, testKeyString, testValueString);
    }

    private <K, V> void validatePublishedRecord(final String topic, final Class<? extends Deserializer<K>> keySerializer, final Class<? extends Deserializer<V>> valueSerializer, final K key, final V value) {
        final List<ConsumerRecord<K, V>> records = sharedKafkaTestResource.getKafkaTestUtils()
            .consumeAllRecordsFromTopic(topic, keySerializer, valueSerializer);

        assertNotNull(records);

        // Look for our match
        boolean foundMatch = false;
        for (final ConsumerRecord<K, V> record : records) {
            foundMatch = (record.key().equals(key) && record.value().equals(value));
            if (foundMatch) {
                break;
            }
        }
        assertTrue("Should have found a match", foundMatch);
    }
}