/**
 * MIT License
 *
 * Copyright (c) 2017 SourceLab.org (https://github.com/Crim/kafka-webview/)
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

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.ClientConfig;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.KafkaResult;
import org.sourcelab.kafka.webview.ui.manager.socket.StartingPosition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Consumer for publishing to a web socket.
 * Intended to be long running.
 */
public class SocketKafkaConsumer implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(SocketKafkaConsumer.class);

    // Define max timeout
    private static final long POLL_TIMEOUT_MS = 3000L;
    private static final long DWELL_TIME_MS = 300L;

    // This defines how many messages can be in the queue before it blocks.
    // Setting this too high means when a consumer hits pause/resume, they'll instantly
    // get a large flood of messages, which maybe is a good thing? Or bad? Unsure.
    private static final int maxQueueCapacity = 25;

    private final KafkaConsumer kafkaConsumer;
    private final ClientConfig clientConfig;
    private final StartingPosition startingPosition;
    private final BlockingQueue<KafkaResult> outputQueue;

    private volatile boolean requestStop = false;

    /**
     * Constructor.
     * @param kafkaConsumer The consumer to consume with.
     * @param clientConfig The client's configuration.
     * @param startingPosition
     */
    public SocketKafkaConsumer(final KafkaConsumer kafkaConsumer, final ClientConfig clientConfig, final StartingPosition startingPosition) {
        this.kafkaConsumer = kafkaConsumer;
        this.clientConfig = clientConfig;
        this.startingPosition = startingPosition;
        this.outputQueue = new LinkedBlockingQueue<>(maxQueueCapacity);
    }

    /**
     * Ask for the next record that the consumer has pulled from kafka.
     * This operation will never block, simply return null if no record found.
     */
    public Optional<KafkaResult> nextResult() {
        // Get the next Result
        return Optional.ofNullable(outputQueue.poll());
    }

    /**
     * Request that the consumer requestStop.
     */
    public void requestStop() {
        this.requestStop = true;
    }

    @Override
    public void run() {
        // Rename thread.
        Thread.currentThread().setName("WebSocket Consumer: " + clientConfig.getConsumerId());
        logger.info("Starting socket consumer for {}", clientConfig.getConsumerId());

        if (startingPosition.isStartFromHead()) {
            toHead();
        } else {
            toTail();
        }

        do {
            // Start trying to consume messages from kafka
            final ConsumerRecords consumerRecords = kafkaConsumer.poll(POLL_TIMEOUT_MS);

            // If no records found
            if (consumerRecords.isEmpty()) {
                // Sleep for a bit
                sleep(POLL_TIMEOUT_MS);

                // Skip to next iteration of loop.
                continue;
            }

            // Push messages onto output queue
            for (final ConsumerRecord consumerRecord : (Iterable<ConsumerRecord>) consumerRecords) {
                // Translate record
                final KafkaResult kafkaResult = new KafkaResult(
                    consumerRecord.partition(),
                    consumerRecord.offset(),
                    consumerRecord.timestamp(),
                    consumerRecord.key(),
                    consumerRecord.value()
                );

                // Add to the queue, this operation may block, effectively preventing the consumer from
                // consuming unbounded-ly.
                try {
                    outputQueue.put(kafkaResult);
                } catch (final InterruptedException interruptedException) {
                    // InterruptedException means we should shut down.
                    requestStop();
                }
            }

            // Sleep for a bit
            sleep(DWELL_TIME_MS);
        }
        while (!requestStop);

        // requestStop
        kafkaConsumer.close();

        logger.info("Shutdown consumer {}", clientConfig.getConsumerId());
    }

    private void sleep(final long timeMs) {
        try {
            // Sleep for a period.
            Thread.sleep(timeMs);
        } catch (final InterruptedException e) {
            requestStop();
        }
    }

    private void toHead() {
        // Get all available partitions
        final List<TopicPartition> topicPartitions = getAllPartitions();

        // Get head offsets for each partition
        final Map<TopicPartition, Long> headOffsets = kafkaConsumer.beginningOffsets(topicPartitions);

        // Loop over each partition
        for (final TopicPartition topicPartition: topicPartitions) {
            final long newOffset = headOffsets.get(topicPartition);
            logger.info("Resetting Partition: {} To Head Offset: {}", topicPartition.partition(), newOffset);

            // Seek to earlier offset
            kafkaConsumer.seek(topicPartition, newOffset);
        }
    }

    private void toTail() {
        // Get all available partitions
        final List<TopicPartition> topicPartitions = getAllPartitions();

        // Get head offsets for each partition
        final Map<TopicPartition, Long> tailOffsets = kafkaConsumer.endOffsets(topicPartitions);

        // Loop over each partition
        for (final TopicPartition topicPartition: topicPartitions) {
            final long newOffset = tailOffsets.get(topicPartition);
            logger.info("Resetting Partition: {} To Tail Offset: {}", topicPartition.partition(), newOffset);

            // Seek to earlier offset
            kafkaConsumer.seek(topicPartition, newOffset);
        }
    }

    private List<TopicPartition> getAllPartitions() {
        // Determine which partitions to subscribe to, for now do all
        final List<PartitionInfo> partitionInfos = kafkaConsumer.partitionsFor(clientConfig.getTopicConfig().getTopicName());

        // Pull out partitions, convert to topic partitions
        final ArrayList<TopicPartition> topicsAndPartitions = new ArrayList<>();
        for (final PartitionInfo partitionInfo : partitionInfos) {
            // Skip filtered partitions
            if (!clientConfig.isPartitionFiltered(partitionInfo.partition())) {
                topicsAndPartitions.add(new TopicPartition(partitionInfo.topic(), partitionInfo.partition()));
            }
        }
        return topicsAndPartitions;
    }
}
