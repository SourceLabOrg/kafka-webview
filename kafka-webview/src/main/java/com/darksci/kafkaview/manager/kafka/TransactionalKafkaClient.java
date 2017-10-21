package com.darksci.kafkaview.manager.kafka;

import com.darksci.kafkaview.manager.kafka.config.ClientConfig;
import com.darksci.kafkaview.manager.kafka.dto.ConsumerState;
import com.darksci.kafkaview.manager.kafka.dto.KafkaResult;
import com.darksci.kafkaview.manager.kafka.dto.KafkaResults;
import com.darksci.kafkaview.manager.kafka.dto.PartitionOffset;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndTimestamp;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TransactionalKafkaClient implements AutoCloseable {
    private final static Logger logger = LoggerFactory.getLogger(TransactionalKafkaClient.class);

    private final KafkaConsumer kafkaConsumer;
    private final ClientConfig clientConfig;
    private List<TopicPartition> cachedTopicsAndPartitions = null;

    public TransactionalKafkaClient(final KafkaConsumer kafkaConsumer, final ClientConfig clientConfig) {
        this.kafkaConsumer = kafkaConsumer;
        this.clientConfig = clientConfig;
    }

    private List<KafkaResult> consume() {
        final List<KafkaResult> kafkaResultList = new ArrayList<>();
        final ConsumerRecords consumerRecords = kafkaConsumer.poll(clientConfig.getPollTimeoutMs());

        logger.info("Consumed {} records", consumerRecords.count());
        final Iterator<ConsumerRecord> recordIterator = consumerRecords.iterator();
        while (recordIterator.hasNext()) {
            final ConsumerRecord consumerRecord = recordIterator.next();
            kafkaResultList.add(
                new KafkaResult(
                    consumerRecord.partition(),
                    consumerRecord.offset(),
                    consumerRecord.timestamp(),
                    consumerRecord.key(),
                    consumerRecord.value()
                )
            );
        }

        // Commit offsets
        commit();
        return kafkaResultList;
    }

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
     * @param timestamp
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

    // Unsure if this should be exposed
    public List<PartitionOffset> getHeadOffsets() {
        final Map<TopicPartition, Long> results = kafkaConsumer.beginningOffsets(getAllPartitions());

        final List<PartitionOffset> offsets = new ArrayList<>();
        for (final Map.Entry<TopicPartition, Long> entry : results.entrySet()) {
            offsets.add(new PartitionOffset(entry.getKey().partition(), entry.getValue()));
        }
        return offsets;
    }

    // Unsure if this should be exposed
    public List<PartitionOffset> getTailOffsets() {
        final Map<TopicPartition, Long> results = kafkaConsumer.endOffsets(getAllPartitions());

        final List<PartitionOffset> offsets = new ArrayList<>();
        for (final Map.Entry<TopicPartition, Long> entry : results.entrySet()) {
            offsets.add(new PartitionOffset(entry.getKey().partition(), entry.getValue()));
        }
        return offsets;
    }


    public ConsumerState getConsumerState() {
        final List<PartitionOffset> offsets = new ArrayList<>();

        for (final TopicPartition topicPartition: getAllPartitions()) {
            final long offset = kafkaConsumer.position(topicPartition);
            offsets.add(new PartitionOffset(topicPartition.partition(), offset));
        }

        return new ConsumerState(clientConfig.getTopicConfig().getTopicName(), offsets);
    }

    // TODO we should probably cache this result.
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

    public void commit() {
        kafkaConsumer.commitSync();
    }

    public void close() {
        kafkaConsumer.close();
    }

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
