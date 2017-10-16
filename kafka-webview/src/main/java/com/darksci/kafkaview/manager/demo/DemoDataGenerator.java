package com.darksci.kafkaview.manager.demo;

import com.darksci.kafkaview.manager.kafka.KafkaOperations;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Just a quick and dirty class to generate some demo kafka data.
 */
public class DemoDataGenerator {
    private final static Logger logger = LoggerFactory.getLogger(DemoDataGenerator.class);

    private final KafkaOperations kafkaOperations;
    private final Map<String, Object> clientConfig;

    public DemoDataGenerator(final KafkaOperations kafkaOperations, final Map<String, Object> clientConfig) {
        this.kafkaOperations = kafkaOperations;
        this.clientConfig = clientConfig;
    }

    public void createDemoTopics() {
        createNumbersTopic();
        createAlphabetTopic();

        // Close out client
        getOperationsClient().close();
    }

    /**
     * This topic has 4 partitions, and we produce numbers 0-2000 into it.
     */
    private void createNumbersTopic() {
        final String topicName = "tennessee-86173.NumberTopic";
        final int numberOfPartitions = 4;
        final short replicationFactor = 1;

        // Debug log
        logger.info("Creating topic {} with {} partitions and replication factor of {}",
            topicName, numberOfPartitions, replicationFactor);

        // Create the topic
        //getOperationsClient().createTopic(topicName, numberOfPartitions, replicationFactor);

        // Create publisher
        final Map<String, Object> config = new HashMap<>();
        config.putAll(clientConfig);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);

        final KafkaProducer<Integer, Integer> producer = new KafkaProducer<>(config);
        for (int index = 0; index < 2000; index++) {
            final int partition = (index % numberOfPartitions);
            producer.send(new ProducerRecord<>(topicName, partition, index, index));
        }
        producer.flush();
        producer.close();
    }

    private void createAlphabetTopic() {
        final String topicName = "tennessee-86173.AlphabetTopic";
        final int numberOfPartitions = 1;
        final short replicationFactor = 1;

        // Debug log
        logger.info("Creating topic {} with {} partitions and replication factor of {}",
            topicName, numberOfPartitions, replicationFactor);

        // Create the topic
        //getOperationsClient().createTopic(topicName, numberOfPartitions, replicationFactor);

        // Create publisher
        final Map<String, Object> config = new HashMap<>();
        config.putAll(clientConfig);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        final KafkaProducer<String, String> producer = new KafkaProducer<>(config);
        for (int repetition = 1; repetition < 11; repetition++) {
            for (int charCode = 65; charCode < 91; charCode++) {
                final char[] key = new char[repetition];
                for (int x = 0; x < repetition; x++) {
                    key[x] = (char) charCode;
                }

                producer.send(new ProducerRecord<>(topicName, new String(key), new String(key)));
            }
        }
        producer.flush();
        producer.close();
    }

    private KafkaOperations getOperationsClient() {
        return kafkaOperations;
    }
}
