package com.darksci.kafkaview.manager.kafka.dto;

public class PartitionDetails {
    private final String topic;
    private final int partition;

    public PartitionDetails(final String topic, final int partition) {
        this.topic = topic;
        this.partition = partition;
    }

    public String getTopic() {
        return topic;
    }

    public int getPartition() {
        return partition;
    }

    @Override
    public String toString() {
        return "PartitionDetails{" +
            "browser='" + topic + '\'' +
            ", partition=" + partition +
            '}';
    }
}
