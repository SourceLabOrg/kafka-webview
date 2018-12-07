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

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.ClientConfig;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.ClusterConfig;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.DeserializerConfig;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.FilterConfig;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.TopicConfig;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.KafkaResults;

import java.util.HashMap;
import java.util.Map;

public class WebKafkaConsumerTest {

    private final static Logger logger = LoggerFactory.getLogger(WebKafkaConsumerTest.class);

    private final KafkaConsumerFactory kafkaConsumerFactory = new KafkaConsumerFactory(
        new KafkaClientConfigUtil("./uploads", "TestPrefix")
    );

    //@Test
    public void doTest() {
        // Create the configs
        final String topicName = "TestTopic";
        final String consumerId = "BobsYerAunty";

        // Defines the Cluster
        final ClusterConfig clusterConfig = ClusterConfig.newBuilder().withBrokerHosts("localhost:9092").build();

        // Create Deserializer Config
        final DeserializerConfig deserializerConfig = DeserializerConfig.newBuilder()
            .withKeyDeserializerClass(StringDeserializer.class)
            .withValueDeserializerClass(StringDeserializer.class)
            .build();

        // Defines our Topic
        final TopicConfig topicConfig = new TopicConfig(clusterConfig, deserializerConfig, topicName);

        // Defines any filters
        final FilterConfig filterConfig = FilterConfig.withNoFilters();

        // Defines our client
        final ClientConfig clientConfig = ClientConfig
            .newBuilder()
            .withTopicConfig(topicConfig)
            .withFilterConfig(filterConfig)
            .withConsumerId(consumerId)
            .build();

        // Build a consumer
        final KafkaConsumer kafkaConsumer = kafkaConsumerFactory.createConsumerAndSubscribe(clientConfig);

        // Create consumer
        final WebKafkaConsumer webKafkaConsumer = new WebKafkaConsumer(kafkaConsumer, clientConfig);

        // Poll
        final KafkaResults results = webKafkaConsumer.consumePerPartition();

        // and close
        webKafkaConsumer.close();

        // Debug log
        logger.info("Consumed Results: {}", results);
    }

    //@Test
    public void publishDummyData() {
        final String topic = "TestTopic";

        // Create publisher
        final Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        final KafkaProducer<String, String> producer = new KafkaProducer<>(config);
        for (int charCode = 65; charCode < 91; charCode++) {
            final char[] key = new char[1];
            key[0] = (char) charCode;

            producer.send(new ProducerRecord<>(topic, new String(key), new String(key)));
        }
        producer.flush();
        producer.close();
    }

//    @Test
    public void publishDummyDataNumbers() {
        final String topic = "NumbersTopic";

        // Create publisher
        final Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        final KafkaProducer<Integer, Integer> producer = new KafkaProducer<>(config);
        for (int value = 0; value < 10000; value++) {
            producer.send(new ProducerRecord<>(topic, value, value));
        }
        producer.flush();
        producer.close();
    }
}