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

import com.salesforce.kafka.test.junit4.SharedKafkaTestResource;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.ClientConfig;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.ClusterConfig;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.DeserializerConfig;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.TopicConfig;

import static org.junit.Assert.assertTrue;

/**
 * Tests currently disabled until we can get an embedded SASL enabled cluster setup.
 * Tests currently assume that there's a local Kafka cluster running at 127.0.0.1:9092 with SASL plain.
 */
public class KafkaConsumerFactorySaslTest {
    private final String brokerHost = "localhost:9092";
    private final String saslUser = "kafkaclient";
    private final String saslPass = "client-secret";

    //@ClassRule
    public static SharedKafkaTestResource sharedKafkaTestResource = new SharedKafkaTestResource();

    /**
     * Factory instance used by tests tests.
     */
    private final KafkaConsumerFactory kafkaConsumerFactory = new KafkaConsumerFactory(
        new KafkaClientConfigUtil(
            "not/used",
            "TestPrefix"
        )
    );

    /**
     * Simple Smoke Test using SASL authentication.
     */
    //@Test
    public void testBasicConsumeWithSaslAuthentication() {
        final int maxRecordsPerPoll = 10;

        final String topicName = "test_topic";

        // Create cluster Config
        final ClusterConfig clusterConfig = ClusterConfig.newBuilder()
            .withBrokerHosts(brokerHost)
            .withUseSasl(true)
            .withSaslPlaintextUsername(saslUser)
            .withSaslPlaintextPassword(saslPass)
            .build();

        // Create Deserializer Config
        final DeserializerConfig deserializerConfig = DeserializerConfig.newBuilder()
            .withKeyDeserializerClass(StringDeserializer.class)
            .withValueDeserializerClass(StringDeserializer.class)
            .build();

        // Create Topic Config
        final TopicConfig topicConfig = new TopicConfig(clusterConfig, deserializerConfig, topicName);

        // Create ClientConfig
        final ClientConfig clientConfig = ClientConfig.newBuilder()
            .withConsumerId("MyConsumerId")
            .withNoFilters()
            .withAllPartitions()
            .withMaxResultsPerPartition(maxRecordsPerPoll)
            .withTopicConfig(topicConfig)
            .build();

        // Create consumer
        try (final KafkaConsumer<String, String> consumer = kafkaConsumerFactory.createConsumerAndSubscribe(clientConfig)) {
            // Attempt to consume, should pull first 10
            ConsumerRecords<String, String> records = consumer.poll(2000L);
            assertTrue("Finish writing this test!" , false);
        }
    }
}
