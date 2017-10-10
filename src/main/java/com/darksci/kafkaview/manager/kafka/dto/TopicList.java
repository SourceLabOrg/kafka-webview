package com.darksci.kafkaview.manager.kafka.dto;

import java.util.List;

public class TopicList {
    private final List<TopicListing> topics;

    public TopicList(final List<TopicListing> topics) {
        this.topics = topics;
    }

    public List<TopicListing> getTopics() {
        return topics;
    }

    @Override
    public String toString() {
        return "TopicList{" +
            "topics=" + topics +
            '}';
    }
}
