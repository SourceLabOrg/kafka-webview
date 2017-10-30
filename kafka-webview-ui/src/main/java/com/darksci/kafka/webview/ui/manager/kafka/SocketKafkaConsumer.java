package com.darksci.kafka.webview.ui.manager.kafka;

import com.darksci.kafka.webview.ui.manager.kafka.config.ClientConfig;
import com.darksci.kafka.webview.ui.manager.kafka.dto.ConsumerState;
import com.darksci.kafka.webview.ui.manager.kafka.dto.KafkaResult;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Consumer for publishing to a websocket.
 */
public class SocketKafkaConsumer implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(SocketKafkaConsumer.class);

    // Define max timeout
    private static final long POLL_TIMEOUT_MS = 3000L;
    private static final long DWELL_TIME_MS = 300L;

    private final KafkaConsumer kafkaConsumer;
    private final ClientConfig clientConfig;
    private final Queue<KafkaResult> outputQueue;

    private volatile boolean requestStop = false;


    /**
     * Constructor.
     * @param kafkaConsumer The consumer to consume with.
     * @param clientConfig The client's configuration.
     * @param outputQueue Where to push consumed messages onto.
     */
    public SocketKafkaConsumer(final KafkaConsumer kafkaConsumer, final ClientConfig clientConfig, final Queue<KafkaResult> outputQueue) {
        this.kafkaConsumer = kafkaConsumer;
        this.clientConfig = clientConfig;
        this.outputQueue = new LinkedBlockingQueue<>(256);
    }

    @Override
    public void run() {
        logger.info("Starting socket consumer for {}", clientConfig.getConsumerId());
        do {
            // Consume messages
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

                // Add to queue, this may block.
                outputQueue.add(kafkaResult);
            }

            // Sleep for a bit
            sleep(DWELL_TIME_MS);
        }
        while (!requestStop);

        // stop
        kafkaConsumer.close();

        logger.info("Shutdown consumer {}", clientConfig.getConsumerId());
    }

    private void sleep(final long timeMs) {
        try {
            // Sleep for a period.
            Thread.sleep(timeMs);
        } catch (final InterruptedException e) {
            stop();
        }
    }

    public KafkaResult nextResult() {
        // Get the next Result
        return outputQueue.poll();
    }

    public void stop() {
        this.requestStop = true;
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

    public void toTail() {
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

    private void commit() {
        kafkaConsumer.commitSync();
    }
}
