package com.darksci.kafkaview.manager.kafka;

import com.darksci.kafkaview.manager.kafka.config.ClientConfig;
import com.darksci.kafkaview.manager.kafka.config.ClusterConfig;
import com.darksci.kafkaview.manager.kafka.config.DeserializerConfig;
import com.darksci.kafkaview.manager.kafka.config.FilterConfig;
import com.darksci.kafkaview.manager.kafka.config.TopicConfig;
import com.darksci.kafkaview.manager.kafka.dto.KafkaResults;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class TransactionalKafkaClientTest {

    private final static Logger logger = LoggerFactory.getLogger(TransactionalKafkaClientTest.class);

    @Test
    public void doTest() {
        // Create the configs
        final String topicName = "TestTopic";
        final String consumerId = "BobsYerAunty";

        // Defines the Cluster
        final ClusterConfig clusterConfig = new ClusterConfig("localhost:9092");

        // Define our Deserializer
        final DeserializerConfig deserializerConfig = new DeserializerConfig(StringDeserializer.class, StringDeserializer.class);

        // Defines our Topic
        final TopicConfig topicConfig = new TopicConfig(clusterConfig, deserializerConfig, topicName);

        // Defines any filters
        final FilterConfig filterConfig = FilterConfig.withNoFilters();

        // Defines our client
        final ClientConfig clientConfig = new ClientConfig(topicConfig, filterConfig, consumerId);

        // Build a consumer
        final KafkaConsumer kafkaConsumer = new KafkaConsumerFactory(clientConfig).createAndSubscribe();

        // Create consumer
        final TransactionalKafkaClient transactionalKafkaClient = new TransactionalKafkaClient(kafkaConsumer, clientConfig);

        // Poll
        final KafkaResults results = transactionalKafkaClient.consumePerPartition();

        // and close
        transactionalKafkaClient.close();

        // Debug log
        logger.info("Consumed Results: {}", results);
    }

    @Test
    public void publishDummyData() {
        final String topic = "TestTopic";

        // Create publisher
        final Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        final KafkaProducer<String, String> producer = new KafkaProducer<>(config);
        for (int charCode = 65; charCode < 91; charCode++) {
            final char[] key = new char[1];
            key[0] = (char) charCode;

            producer.send(new ProducerRecord<>(topic, new String(key), new String(key)));
        }
        producer.flush();
        producer.close();
    }

    @Test
    public void publishDummyDataNumbers() {
        final String topic = "NumbersTopic";

        // Create publisher
        final Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        final KafkaProducer<Integer, Integer> producer = new KafkaProducer<>(config);
        for (int value = 0; value < 1000; value++) {
            producer.send(new ProducerRecord<>(topic, value, value));
        }
        producer.flush();
        producer.close();
    }

}