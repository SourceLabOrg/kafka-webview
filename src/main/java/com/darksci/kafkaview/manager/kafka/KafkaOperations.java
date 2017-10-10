package com.darksci.kafkaview.manager.kafka;

import com.darksci.kafkaview.manager.kafka.config.ClientConfig;
import com.darksci.kafkaview.manager.kafka.config.ClusterConfig;
import com.darksci.kafkaview.manager.kafka.config.DeserializerConfig;
import com.darksci.kafkaview.manager.kafka.config.FilterConfig;
import com.darksci.kafkaview.manager.kafka.config.TopicConfig;
import com.darksci.kafkaview.manager.kafka.dto.PartitionDetails;
import com.darksci.kafkaview.manager.kafka.dto.TopicDetails;
import com.darksci.kafkaview.manager.kafka.dto.TopicList;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class KafkaOperations implements AutoCloseable {
    private final static Logger logger = LoggerFactory.getLogger(KafkaOperations.class);
    private final static long TIMEOUT = 5000L;

    private final KafkaConsumer kafkaConsumer;

    public KafkaOperations(final KafkaConsumer kafkaConsumer) {
        this.kafkaConsumer = kafkaConsumer;
    }

    /**
     * Retrieve all available topics within cluster.
     */
    public TopicList getAvailableTopics() {
        final Map<String, List<PartitionInfo>> allTopics = kafkaConsumer.listTopics();

        final List<TopicDetails> topicDetails = new ArrayList<>();

        // Loop over
        for (final Map.Entry<String, List<PartitionInfo>> entry: allTopics.entrySet()) {
            final String topicName = entry.getKey();
            final List<PartitionInfo> partitionInfos = entry.getValue();

            // Loop over each partition
            final List<PartitionDetails> partitionDetails = new ArrayList<>();
            for (final PartitionInfo partitionInfo: partitionInfos) {
                partitionDetails.add(new PartitionDetails(partitionInfo.topic(), partitionInfo.partition()));
            }
            topicDetails.add(new TopicDetails(topicName, partitionDetails));
        }

        return new TopicList(topicDetails);
    }

    public void getClusterInfo() {
        
    }

    public void close() {
        kafkaConsumer.close();
    }

    public static KafkaOperations newKafkaOperationalInstance(final String kafkaBrokers) {
        final ClusterConfig clusterConfig = new ClusterConfig(kafkaBrokers);
        final DeserializerConfig deserializerConfig = DeserializerConfig.defaultConfig();
        final TopicConfig topicConfig = new TopicConfig(clusterConfig, deserializerConfig, "NotUsed");

        final ClientConfig clientConfig = new ClientConfig(topicConfig, FilterConfig.withNoFilters(), "BobsYerAunty");
        return new KafkaOperations(new KafkaConsumerFactory(clientConfig).create());
    }

}
