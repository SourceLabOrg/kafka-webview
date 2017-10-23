package com.darksci.kafka.webview.ui.manager.kafka.dto;

import java.util.Collections;
import java.util.List;

public class KafkaResults {
    private final List<KafkaResult> results;
    private final List<PartitionOffset> consumerOffsets;
    private final List<PartitionOffset> headOffsets;
    private final List<PartitionOffset> tailOffsets;
    private final int numberOfRecords;

    public KafkaResults(
        final List<KafkaResult> results,
        final List<PartitionOffset> consumerOffsets,
        final List<PartitionOffset> headOffsets,
        final List<PartitionOffset> tailOffsets) {
        this.results = Collections.unmodifiableList(results);
        this.numberOfRecords = results.size();
        this.consumerOffsets = Collections.unmodifiableList(consumerOffsets);
        this.headOffsets = Collections.unmodifiableList(headOffsets);
        this.tailOffsets = Collections.unmodifiableList(tailOffsets);
    }

    public List<KafkaResult> getResults() {
        return results;
    }


    public int getNumberOfRecords() {
        return numberOfRecords;
    }

    public List<PartitionOffset> getConsumerOffsets() {
        return consumerOffsets;
    }

    public List<PartitionOffset> getHeadOffsets() {
        return headOffsets;
    }

    public List<PartitionOffset> getTailOffsets() {
        return tailOffsets;
    }

    @Override
    public String toString() {
        return "KafkaResults{" +
            "results=" + results +
            ", consumerOffsets=" + consumerOffsets +
            ", headOffsets=" + headOffsets +
            ", tailOffsets=" + tailOffsets +
            ", numberOfRecords=" + numberOfRecords +
            '}';
    }
}
