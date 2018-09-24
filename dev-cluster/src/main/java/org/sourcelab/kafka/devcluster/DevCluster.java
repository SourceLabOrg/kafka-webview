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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        final KafkaTestUtils utils = new KafkaTestUtils(kafkaTestCluster);
        utils.createTopic(topicName, clusterSize, (short) clusterSize);

        // Publish some data into that topic
        for (int partition = 0; partition < clusterSize; partition++) {
            utils.produceRecords(1000, topicName, partition);
        }

        kafkaTestCluster
            .getKafkaBrokers()
            .stream()
            .forEach((broker) -> logger.info("Started broker with Id {} at {}", broker.getBrokerId(), broker.getConnectString()));

        logger.info("Cluster started at: {}", kafkaTestCluster.getKafkaConnectString());

        // Wait forever.
        Thread.currentThread().join();
    }
}
