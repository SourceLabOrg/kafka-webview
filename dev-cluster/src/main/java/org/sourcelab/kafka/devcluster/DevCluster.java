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

package org.sourcelab.kafka.devcluster;

import com.salesforce.kafka.test.KafkaTestCluster;
import com.salesforce.kafka.test.KafkaTestUtils;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Properties;

/**
 * Simple applicatin for firing up a Kafka cluster of 1 or more nodes.  This exists solely
 * because standing up a real multi-node kafka cluster is more complicated than putting this together.
 *
 * Not intended for external consumption.
 */
public class DevCluster {
    private static final Logger logger = LoggerFactory.getLogger(DevCluster.class);

    /**
     * Main entry point
     * @param args command line args.
     */
    public static void main(final String[] args) throws Exception {
        // Right now we accept one parameter, the number of nodes in the cluster.
        final int clusterSize;
        if (args.length > 0) {
            clusterSize = Integer.parseInt(args[0]);
        } else {
            clusterSize = 1;
        }

        logger.info("Starting up kafka cluster with {} brokers", clusterSize);

        // Create a test cluster
        final KafkaTestCluster kafkaTestCluster = new KafkaTestCluster(clusterSize);

        // Start the cluster.
        kafkaTestCluster.start();

        // Create a topic
        final String topicName = "TestTopicA";
        final int partitionsCount = 5 * clusterSize;
        final KafkaTestUtils utils = new KafkaTestUtils(kafkaTestCluster);
        utils.createTopic(topicName, partitionsCount, (short) clusterSize);

        // Publish some data into that topic
        for (int partition = 0; partition < partitionsCount; partition++) {
            utils.produceRecords(1000, topicName, partition);
        }

        kafkaTestCluster
            .getKafkaBrokers()
            .stream()
            .forEach((broker) -> {
                logger.info("Started broker with Id {} at {}", broker.getBrokerId(), broker.getConnectString());
            });

        logger.info("Cluster started at: {}", kafkaTestCluster.getKafkaConnectString());

        runEndlessConsumer(topicName, utils);
        runEndlessProducer(topicName, partitionsCount, utils);

        // Wait forever.
        Thread.currentThread().join();
    }

    /**
     * Fire up a new thread running an endless producer script into the given topic and partitions.
     * @param topicName Name of the topic to produce records into.
     * @param partitionCount number of partitions that exist on that topic.
     * @param utils KafkaUtils instance.
     */
    private static void runEndlessProducer(
        final String topicName,
        final int partitionCount,
        final KafkaTestUtils utils
    ) {
        final Thread producerThread = new Thread(() -> {
            do {

                // Publish some data into that topic
                for (int partition = 0; partition < partitionCount; partition++) {
                    utils.produceRecords(1000, topicName, partition);
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException e) {
                    return;
                }
            } while (true);
        });
        producerThread.start();

    }

    /**
     * Fire up a new thread running an enless consumer script that reads from the given topic.
     * @param topicName Topic to consume from.
     * @param utils KafkaUtils instance.
     */
    private static void runEndlessConsumer(final String topicName, final KafkaTestUtils utils) {
        final Thread consumerThread = new Thread(() -> {
            // Start a consumer
            final Properties properties = new Properties();
            properties.put("max.poll.records", 37);
            properties.put("group.id", "MyConsumerId");

            try (final KafkaConsumer<String, String> consumer
                     = utils.getKafkaConsumer(StringDeserializer.class, StringDeserializer.class, properties)) {

                consumer.subscribe(Collections.singleton(topicName));
                do {
                    final ConsumerRecords<String, String> records = consumer.poll(1000);
                    consumer.commitSync();

                    logger.info("Consumed {} records", records.count());

                    if (records.isEmpty()) {
                        consumer.seekToBeginning(consumer.assignment());
                        consumer.commitSync();
                    }
                    Thread.sleep(1000);
                } while (true);

            } catch (final InterruptedException e) {
                return;
            }
        });
        consumerThread.start();
    }
}
