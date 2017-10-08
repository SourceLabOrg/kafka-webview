package com.darksci.kafkaview.manager.kafka.dto;

import java.util.List;

public class KafkaResults {
    private final List<KafkaResult> results;

    public KafkaResults(final List<KafkaResult> results) {
        this.results = results;
    }

    public List<KafkaResult> getResults() {
        return results;
    }

    @Override
    public String toString() {
        return "KafkaResults{" +
            "results=" + results +
            '}';
    }
}
