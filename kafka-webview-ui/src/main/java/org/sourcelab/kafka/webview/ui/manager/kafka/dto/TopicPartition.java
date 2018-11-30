package org.sourcelab.kafka.webview.ui.manager.kafka.dto;

import java.util.Objects;

/**
 * Represents a partition on a specific topic.
 */
public class TopicPartition {
    private final String topic;
    private final int partition;

    /**
     * Constructor.
     * @param topic Name of the topic.
     * @param partition partition id.
     */
    public TopicPartition(final String topic, final int partition) {
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
        return "TopicPartition{"
            + "topic='" + topic + '\''
            + ", partition=" + partition
            + '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final TopicPartition that = (TopicPartition) o;
        return partition == that.partition &&
            Objects.equals(topic, that.topic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topic, partition);
    }
}
