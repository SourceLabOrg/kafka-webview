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
import org.apache.kafka.clients.consumer.OffsetAndTimestamp;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.ClientConfig;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.ConsumerState;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.KafkaResult;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.KafkaResults;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.PartitionOffset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Wrapper around KafkaConsumer.  This instance is intended to be short lived and only live
 * during the life-time of a single web request.
 */
public class WebKafkaConsumer implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(WebKafkaConsumer.class);

    private final KafkaConsumer kafkaConsumer;
    private final ClientConfig clientConfig;
    private List<TopicPartition> cachedTopicsAndPartitions = null;

    /**
     * Constructor.
     * @param kafkaConsumer The underlying/wrapped KafkaConsumer instance.
     * @param clientConfig The client configuration.
     */
    public WebKafkaConsumer(final KafkaConsumer kafkaConsumer, final ClientConfig clientConfig) {
        this.kafkaConsumer = kafkaConsumer;
        this.clientConfig = clientConfig;
    }

    private List<KafkaResult> consume() {
        final List<KafkaResult> kafkaResultList = new ArrayList<>();
        final ConsumerRecords consumerRecords = kafkaConsumer.poll(clientConfig.getPollTimeoutMs());

        logger.info("Consumed {} records", consumerRecords.count());
        final Iterator<ConsumerRecord> recordIterator = consumerRecords.iterator();
        while (recordIterator.hasNext()) {
            // Get next record
            final ConsumerRecord consumerRecord = recordIterator.next();

            // Convert to KafkaResult.
            final KafkaResult kafkaResult = new KafkaResult(
                consumerRecord.partition(),
                consumerRecord.offset(),
                consumerRecord.timestamp(),
                consumerRecord.key(),
                consumerRecord.value()
            );

            // Add to list.
            kafkaResultList.add(kafkaResult);
        }

        // Commit offsets
        commit();
        return kafkaResultList;
    }

    /**
     * Retrieves next batch of records per partition.
     * @return KafkaResults object containing any found records.
     */
    public KafkaResults consumePerPartition() {
        final Map<Integer, List<KafkaResult>> resultsByPartition = new TreeMap<>();
        for (final TopicPartition topicPartition: getAllPartitions()) {
            // Subscribe to just that topic partition
            kafkaConsumer.assign(Collections.singleton(topicPartition));

            // consume
            final List<KafkaResult> kafkaResults = consume();

            logger.info("Consumed Partition {} Records: {}", topicPartition.partition(), kafkaResults.size());
            resultsByPartition.put(topicPartition.partition(), kafkaResults);
        }

        // Reassign all partitions
        kafkaConsumer.assign(getAllPartitions());

        // Loop over results
        final List<KafkaResult> allResults = new ArrayList<>();
        for (final List<KafkaResult> results: resultsByPartition.values()) {
            allResults.addAll(results);
        }

        // Create return object
        return new KafkaResults(
            allResults,
            getConsumerState().getOffsets(),
            getHeadOffsets(),
            getTailOffsets()
        );
    }

    /**
     * Seek to the specified offsets.
     * @param partitionOffsetMap Map of PartitionId => Offset to seek to.
     * @return ConsumerState representing the consumer's positions.
     */
    public ConsumerState seek(final Map<Integer, Long> partitionOffsetMap) {
        for (final Map.Entry<Integer, Long> entry: partitionOffsetMap.entrySet()) {
            kafkaConsumer.seek(
                new TopicPartition(clientConfig.getTopicConfig().getTopicName(), entry.getKey()),
                entry.getValue()
            );
        }
        commit();
        return getConsumerState();
    }

    /**
     * Seek consumer to specific timestamp
     * @param timestamp Unix timestamp in milliseconds to seek to.
     */
    public ConsumerState seek(final long timestamp) {
        // Find offsets for timestamp
        final Map<TopicPartition, Long> timestampMap = new HashMap<>();
        for (final TopicPartition topicPartition: getAllPartitions()) {
            timestampMap.put(topicPartition, timestamp);
        }
        final Map<TopicPartition, OffsetAndTimestamp> offsetMap = kafkaConsumer.offsetsForTimes(timestampMap);

        // Build map of partition => offset
        final Map<Integer, Long> partitionOffsetMap = new HashMap<>();
        for (Map.Entry<TopicPartition, OffsetAndTimestamp> entry: offsetMap.entrySet()) {
            partitionOffsetMap.put(entry.getKey().partition(), entry.getValue().offset());
        }

        // Now lets seek to those offsets
        return seek(partitionOffsetMap);
    }

    private List<PartitionOffset> getHeadOffsets() {
        final Map<TopicPartition, Long> results = kafkaConsumer.beginningOffsets(getAllPartitions());

        final List<PartitionOffset> offsets = new ArrayList<>();
        for (final Map.Entry<TopicPartition, Long> entry : results.entrySet()) {
            offsets.add(new PartitionOffset(entry.getKey().partition(), entry.getValue()));
        }
        return offsets;
    }

    private List<PartitionOffset> getTailOffsets() {
        final Map<TopicPartition, Long> results = kafkaConsumer.endOffsets(getAllPartitions());

        final List<PartitionOffset> offsets = new ArrayList<>();
        for (final Map.Entry<TopicPartition, Long> entry : results.entrySet()) {
            offsets.add(new PartitionOffset(entry.getKey().partition(), entry.getValue()));
        }
        return offsets;
    }

    private ConsumerState getConsumerState() {
        final List<PartitionOffset> offsets = new ArrayList<>();

        for (final TopicPartition topicPartition: getAllPartitions()) {
            final long offset = kafkaConsumer.position(topicPartition);
            offsets.add(new PartitionOffset(topicPartition.partition(), offset));
        }

        return new ConsumerState(clientConfig.getTopicConfig().getTopicName(), offsets);
    }

    private List<TopicPartition> getAllPartitions() {
        // If we have not pulled this yet
        if (cachedTopicsAndPartitions == null) {
            // Determine which partitions to subscribe to, for now do all
            final List<PartitionInfo> partitionInfos = kafkaConsumer.partitionsFor(clientConfig.getTopicConfig().getTopicName());

            // Pull out partitions, convert to topic partitions
            cachedTopicsAndPartitions = new ArrayList<>();
            for (final PartitionInfo partitionInfo : partitionInfos) {
                // Skip filtered partitions
                if (!clientConfig.isPartitionFiltered(partitionInfo.partition())) {
                    cachedTopicsAndPartitions.add(new TopicPartition(partitionInfo.topic(), partitionInfo.partition()));
                }
            }
        }
        return cachedTopicsAndPartitions;
    }

    private void commit() {
        kafkaConsumer.commitSync();
    }

    /**
     * Closes out the consumer.
     */
    public void close() {
        kafkaConsumer.close();
    }

    /**
     * Seek to the previous 'page' of records.
     */
    public void previous() {
        // Get all available partitions
        final List<TopicPartition> topicPartitions = getAllPartitions();

        // Get head offsets for each partition
        final Map<TopicPartition, Long> headOffsets = kafkaConsumer.beginningOffsets(topicPartitions);

        // Loop over each partition
        for (final TopicPartition topicPartition: topicPartitions) {
            // Calculate our previous offsets
            final long headOffset = headOffsets.get(topicPartition);
            final long currentOffset = kafkaConsumer.position(topicPartition);
            long newOffset = currentOffset - (clientConfig.getMaxResultsPerPartition() * 2);

            // Can't go before the head position!
            if (newOffset < headOffset) {
                newOffset = headOffset;
            }

            logger.info("Partition: {} Previous Offset: {} New Offset: {}", topicPartition.partition(), currentOffset, newOffset);

            // Seek to earlier offset
            kafkaConsumer.seek(topicPartition, newOffset);
        }
        commit();
    }

    /**
     * Seek to the next 'page' of records.
     */
    public void next() {
        // Get all available partitions
        final List<TopicPartition> topicPartitions = getAllPartitions();

        // Get head offsets for each partition
        final Map<TopicPartition, Long> tailOffsets = kafkaConsumer.endOffsets(topicPartitions);

        // Loop over each partition
        for (final TopicPartition topicPartition: topicPartitions) {
            // Calculate our previous offsets
            final long tailOffset = tailOffsets.get(topicPartition);
            final long currentOffset = kafkaConsumer.position(topicPartition);
            long newOffset = currentOffset + clientConfig.getMaxResultsPerPartition();

            if (newOffset < tailOffset) {
                newOffset = tailOffset;
            }
            logger.info("Partition: {} Previous Offset: {} New Offset: {}", topicPartition.partition(), currentOffset, newOffset);

            // Seek to earlier offset
            kafkaConsumer.seek(topicPartition, newOffset);
        }
        commit();
    }

    /**
     * Seek to the HEAD of a topic.
     */
    public ConsumerState toHead() {
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
        commit();

        return getConsumerState();
    }

    /**
     * Seek to the TAIL of a topic.
     */
    public ConsumerState toTail() {
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
        commit();

        return getConsumerState();
    }
}
