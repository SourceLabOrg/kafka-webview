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

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Wrapper around KafkaConsumer.  This instance is intended to be short lived and only live
 * during the life-time of a single web request.
 *
 * In order to provide a relatively "sane" ability to "page" through results in a consistent way, this
 * consumes from each partition in parallel and merges the results.
 *
 * The parallelization factor is determined by the ExecutorService provided to the constructor.
 */
public class ParallelWebKafkaConsumer implements WebKafkaConsumer {

    private static final Logger logger = LoggerFactory.getLogger(ParallelWebKafkaConsumer.class);

    private final KafkaConsumerFactory kafkaConsumerFactory;
    private final ClientConfig clientConfig;
    private final Duration pollTimeoutDuration;
    private final ExecutorService executorService;

    /**
     * Cached topic partition details so they do not need to be retrieved more than once.
     */
    private List<TopicPartition> cachedTopicsAndPartitions = null;

    /**
     * KafkaConsumer instance used to perform seek and state operations.
     * Not used for consuming records.
     */
    private KafkaConsumer coordinatorConsumer = null;

    /**
     * Constructor.
     * @param kafkaConsumerFactory Factor for creating KafkaConsumer instances.
     * @param clientConfig Client configuration.
     * @param executorService ExecutorService to submit parallel consuming tasks to.
     */
    public ParallelWebKafkaConsumer(
        final KafkaConsumerFactory kafkaConsumerFactory,
        final ClientConfig clientConfig,
        final ExecutorService executorService
    ) {
        this.kafkaConsumerFactory = kafkaConsumerFactory;
        this.clientConfig = clientConfig;
        this.pollTimeoutDuration = Duration.ofMillis(clientConfig.getPollTimeoutMs());
        this.executorService = executorService;
    }

    @Override
    public KafkaResults consumePerPartition() {

        final List<TopicPartition> allTopicPartitions = getAllPartitions(getCoordinatorConsumer());

        // To preserve order
        final Map<Integer, CompletableFuture<List<KafkaResult>>> completableFuturesByPartition = new TreeMap<>();

        // Loop over each topic partition
        for (final TopicPartition topicPartition : allTopicPartitions) {

            // Create a new async task
            final CompletableFuture<List<KafkaResult>> future = CompletableFuture.supplyAsync(() -> {
                // Create a new consumer for each task
                try (final KafkaConsumer perTopicConsumer = createNewConsumer()) {
                    // Subscribe to just the single topic partition
                    perTopicConsumer.assign(Collections.singleton(topicPartition));

                    // consume messages from that partition
                    return consume(perTopicConsumer);
                }
            }, executorService);

            // Keep references to our ASync Tasks
            completableFuturesByPartition.put(topicPartition.partition(), future);
        }

        // Merge results.
        final List<KafkaResult> allResults = new ArrayList<>();
        completableFuturesByPartition.forEach((partition, future) -> {
            allResults.addAll(future.join());
        });

        // Create return object
        return new KafkaResults(
            allResults,
            getConsumerState(getCoordinatorConsumer()).getOffsets(),
            getHeadOffsets(getCoordinatorConsumer()),
            getTailOffsets(getCoordinatorConsumer())
        );
    }

    @Override
    public ConsumerState seek(final Map<Integer, Long> partitionOffsetMap) {

        for (final Map.Entry<Integer, Long> entry : partitionOffsetMap.entrySet()) {
            getCoordinatorConsumer().seek(
                new TopicPartition(clientConfig.getTopicConfig().getTopicName(), entry.getKey()),
                entry.getValue()
            );
        }
        commit(getCoordinatorConsumer());
        return getConsumerState(getCoordinatorConsumer());
    }

    @Override
    public ConsumerState seek(final long timestamp) {

        // Find offsets for timestamp
        final Map<TopicPartition, Long> timestampMap = new HashMap<>();
        for (final TopicPartition topicPartition : getAllPartitions(getCoordinatorConsumer())) {
            timestampMap.put(topicPartition, timestamp);
        }
        final Map<TopicPartition, OffsetAndTimestamp> offsetMap = getCoordinatorConsumer().offsetsForTimes(timestampMap);

        // Build map of partition => offset
        final Map<Integer, Long> partitionOffsetMap = new HashMap<>();
        for (Map.Entry<TopicPartition, OffsetAndTimestamp> entry : offsetMap.entrySet()) {
            partitionOffsetMap.put(entry.getKey().partition(), entry.getValue().offset());
        }

        // Now lets seek to those offsets
        return seek(partitionOffsetMap);
    }

    @Override
    public void close() {
        // If we have a coordinator consumer instance, we should close it.
        // Avoid using the getter method as it will create the instance if it doesn't exist.
        if (coordinatorConsumer != null) {
            coordinatorConsumer.close();
            coordinatorConsumer = null;
        }
    }

    @Override
    public void previous() {
        // Get all available partitions
        final List<TopicPartition> topicPartitions = getAllPartitions(getCoordinatorConsumer());

        // Get head offsets for each partition
        final Map<TopicPartition, Long> headOffsets = getCoordinatorConsumer().beginningOffsets(topicPartitions);

        // Loop over each partition
        for (final TopicPartition topicPartition : topicPartitions) {
            // Calculate our previous offsets
            final long headOffset = headOffsets.get(topicPartition);
            final long currentOffset = getCoordinatorConsumer().position(topicPartition);
            long newOffset = currentOffset - (clientConfig.getMaxResultsPerPartition() * 2);

            // Can't go before the head position!
            if (newOffset < headOffset) {
                newOffset = headOffset;
            }

            logger.info("Partition: {} Previous Offset: {} New Offset: {}", topicPartition.partition(), currentOffset, newOffset);

            // Seek to earlier offset
            getCoordinatorConsumer().seek(topicPartition, newOffset);
        }
        commit(getCoordinatorConsumer());
    }

    @Override
    public void next() {
        // Get all available partitions
        final List<TopicPartition> topicPartitions = getAllPartitions(getCoordinatorConsumer());

        // Get head offsets for each partition
        final Map<TopicPartition, Long> tailOffsets = getCoordinatorConsumer().endOffsets(topicPartitions);

        // Loop over each partition
        for (final TopicPartition topicPartition : topicPartitions) {
            // Calculate our previous offsets
            final long tailOffset = tailOffsets.get(topicPartition);
            final long currentOffset = getCoordinatorConsumer().position(topicPartition);
            long newOffset = currentOffset + clientConfig.getMaxResultsPerPartition();

            if (newOffset < tailOffset) {
                newOffset = tailOffset;
            }
            logger.info("Partition: {} Previous Offset: {} New Offset: {}", topicPartition.partition(), currentOffset, newOffset);

            // Seek to earlier offset
            getCoordinatorConsumer().seek(topicPartition, newOffset);
        }
        commit(getCoordinatorConsumer());
    }

    @Override
    public ConsumerState toHead() {
        // Get all available partitions
        final List<TopicPartition> topicPartitions = getAllPartitions(getCoordinatorConsumer());

        // Get head offsets for each partition
        final Map<TopicPartition, Long> headOffsets = getCoordinatorConsumer().beginningOffsets(topicPartitions);

        // Loop over each partition
        for (final TopicPartition topicPartition : topicPartitions) {
            final long newOffset = headOffsets.get(topicPartition);
            logger.info("Resetting Partition: {} To Head Offset: {}", topicPartition.partition(), newOffset);

            // Seek to earlier offset
            getCoordinatorConsumer().seek(topicPartition, newOffset);
        }
        commit(getCoordinatorConsumer());
        return getConsumerState(getCoordinatorConsumer());
    }

    @Override
    public ConsumerState toTail() {
        // Get all available partitions
        final List<TopicPartition> topicPartitions = getAllPartitions(getCoordinatorConsumer());

        // Get head offsets for each partition
        final Map<TopicPartition, Long> tailOffsets = getCoordinatorConsumer().endOffsets(topicPartitions);

        // Loop over each partition
        for (final TopicPartition topicPartition : topicPartitions) {
            final long newOffset = tailOffsets.get(topicPartition);
            logger.info("Resetting Partition: {} To Tail Offset: {}", topicPartition.partition(), newOffset);

            // Seek to earlier offset
            getCoordinatorConsumer().seek(topicPartition, newOffset);
        }
        commit(getCoordinatorConsumer());

        return getConsumerState(getCoordinatorConsumer());
    }

    /**
     * Get or create the Coordinator Consumer instance.
     * Not intended to be used to consume records.
     * @return KafkaConsumer instance.
     */
    private KafkaConsumer getCoordinatorConsumer()
    {
        if (coordinatorConsumer == null) {
            // Create new consumer and assign to all partitions.
            coordinatorConsumer = createNewConsumer();
            coordinatorConsumer.assign(getAllPartitions(coordinatorConsumer));
        }
        return coordinatorConsumer;
    }

    /**
     * Creates a new consumer, but does NOT subscribe to any partitions.
     * Will be required to subscribe to the partitions manually you require.
     * @return KafkaConsumer
     */
    private KafkaConsumer createNewConsumer() {
        return kafkaConsumerFactory.createConsumer(clientConfig);
    }

    private ConsumerState getConsumerState(final KafkaConsumer kafkaConsumer) {
        final List<PartitionOffset> offsets = new ArrayList<>();

        for (final TopicPartition topicPartition: getAllPartitions(kafkaConsumer)) {
            final long offset = kafkaConsumer.position(topicPartition);
            offsets.add(new PartitionOffset(topicPartition.partition(), offset));
        }

        return new ConsumerState(clientConfig.getTopicConfig().getTopicName(), offsets);
    }

    /**
     * Mark synchronized to prevent multi-threaded weirdness.
     */
    private List<TopicPartition> getAllPartitions(final KafkaConsumer kafkaConsumer) {
        // If we have not pulled this yet
        if (cachedTopicsAndPartitions == null) {
            // Attempt to prevent multi-threaded weirdness.
            synchronized (this) {
                if (cachedTopicsAndPartitions == null) {
                    // Determine which partitions to subscribe to, for now do all
                    final List<PartitionInfo> partitionInfos = kafkaConsumer.partitionsFor(clientConfig.getTopicConfig().getTopicName());

                    // Pull out partitions, convert to topic partitions
                    final List<TopicPartition> tempHolder = new ArrayList<>();
                    for (final PartitionInfo partitionInfo : partitionInfos) {
                        // Skip filtered partitions
                        if (!clientConfig.isPartitionFiltered(partitionInfo.partition())) {
                            tempHolder.add(new TopicPartition(partitionInfo.topic(), partitionInfo.partition()));
                        }
                    }
                    cachedTopicsAndPartitions = Collections.unmodifiableList(tempHolder);
                }
            }

        }
        return cachedTopicsAndPartitions;
    }

    private List<PartitionOffset> getHeadOffsets(final KafkaConsumer kafkaConsumer) {
        final Map<TopicPartition, Long> results = kafkaConsumer.beginningOffsets(getAllPartitions(kafkaConsumer));

        final List<PartitionOffset> offsets = new ArrayList<>();
        for (final Map.Entry<TopicPartition, Long> entry : results.entrySet()) {
            offsets.add(new PartitionOffset(entry.getKey().partition(), entry.getValue()));
        }
        return offsets;
    }

    private List<PartitionOffset> getTailOffsets(final KafkaConsumer kafkaConsumer) {
        final Map<TopicPartition, Long> results = kafkaConsumer.endOffsets(getAllPartitions(kafkaConsumer));

        final List<PartitionOffset> offsets = new ArrayList<>();
        for (final Map.Entry<TopicPartition, Long> entry : results.entrySet()) {
            offsets.add(new PartitionOffset(entry.getKey().partition(), entry.getValue()));
        }
        return offsets;
    }


    private void commit(final KafkaConsumer kafkaConsumer) {
        kafkaConsumer.commitSync();
    }

    private List<KafkaResult> consume(final KafkaConsumer kafkaConsumer) {
        final List<KafkaResult> kafkaResultList = new ArrayList<>();
        final ConsumerRecords consumerRecords = kafkaConsumer.poll(pollTimeoutDuration);

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
        commit(kafkaConsumer);
        return kafkaResultList;
    }
}
