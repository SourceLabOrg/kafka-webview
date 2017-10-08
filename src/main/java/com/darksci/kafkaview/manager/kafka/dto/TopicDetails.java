package com.darksci.kafkaview.manager.kafka.dto;

import java.util.List;

public class TopicDetails {
    private final String name;
    private final List<PartitionDetails> partitions;

    public TopicDetails(final String name, final List<PartitionDetails> partitions) {
        this.name = name;
        this.partitions = partitions;
    }

    public String getName() {
        return name;
    }

    public List<PartitionDetails> getPartitions() {
        return partitions;
    }

    @Override
    public String toString() {
        return "TopicDetails{" +
            "name='" + name + '\'' +
            ", partitions=" + partitions +
            '}';
    }
}
