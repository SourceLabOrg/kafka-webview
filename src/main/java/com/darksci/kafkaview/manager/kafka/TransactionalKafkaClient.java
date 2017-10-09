package com.darksci.kafkaview.manager.kafka;

import com.darksci.kafkaview.manager.kafka.config.ClientConfig;
import com.darksci.kafkaview.manager.kafka.dto.KafkaResult;
import com.darksci.kafkaview.manager.kafka.dto.KafkaResults;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
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
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;

public class TransactionalKafkaClient implements AutoCloseable {
    private final static Logger logger = LoggerFactory.getLogger(TransactionalKafkaClient.class);
    private final static long TIMEOUT = 1000L;

    private final KafkaConsumer kafkaConsumer;
    private final ClientConfig clientConfig;

    public TransactionalKafkaClient(final KafkaConsumer kafkaConsumer, final ClientConfig clientConfig) {
        this.kafkaConsumer = kafkaConsumer;
        this.clientConfig = clientConfig;
    }

    public KafkaResults consume() {
        final List<KafkaResult> kafkaResultList = new ArrayList<>();
        final ConsumerRecords consumerRecords = kafkaConsumer.poll(TIMEOUT);

        logger.info("Consumed {} records", consumerRecords.count());
        final Iterator<ConsumerRecord> recordIterator = consumerRecords.iterator();
        while (recordIterator.hasNext()) {
            final ConsumerRecord consumerRecord = recordIterator.next();
            kafkaResultList.add(
                new KafkaResult(
                    consumerRecord.partition(),
                    consumerRecord.offset(),
                    consumerRecord.key(),
                    consumerRecord.value()
                )
            );
        }
        // Commit offsets
        commit();
        return new KafkaResults(kafkaResultList);
    }

    public KafkaResults consumePerPartition() {
        final Map<Integer, List<KafkaResult>> resultsByPartition = new TreeMap<>();

        for (final TopicPartition topicPartition: getAllPartitions()) {
            // Subscribe to just that topic partition
            kafkaConsumer.assign(Collections.singleton(topicPartition));

            // consume
            final KafkaResults kafkaResults = consume();

            logger.info("Consumed Partition {} Records: {}", topicPartition.partition(), kafkaResults.getResults().size());

            resultsByPartition.put(topicPartition.partition(), kafkaResults.getResults());
        }

        // Loop over results
        final List<KafkaResult> allResults = new ArrayList<>();
        for (final List<KafkaResult> results: resultsByPartition.values()) {
            allResults.addAll(results);
        }

        return new KafkaResults(allResults);
    }

    private List<TopicPartition> getAllPartitions() {
        // Determine which partitions to subscribe to, for now do all
        final List<PartitionInfo> partitionInfos = kafkaConsumer.partitionsFor(clientConfig.getTopicConfig().getTopicName());

        // Pull out partitions, convert to browser partitions
        final List<TopicPartition> topicPartitions = new ArrayList<>();
        for (final PartitionInfo partitionInfo: partitionInfos) {
            topicPartitions.add(new TopicPartition(partitionInfo.topic(), partitionInfo.partition()));
        }
        return topicPartitions;
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
            long newOffset = currentOffset - (clientConfig.getMaxRecords() * 2);

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
            long newOffset = currentOffset + clientConfig.getMaxRecords();

            if (newOffset < tailOffset) {
                newOffset = tailOffset;
            }
            logger.info("Partition: {} Previous Offset: {} New Offset: {}", topicPartition.partition(), currentOffset, newOffset);

            // Seek to earlier offset
            kafkaConsumer.seek(topicPartition, newOffset);
        }
        commit();
    }

    public void toHead() {
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
    }
}
