package com.darksci.kafka.webview.manager.kafka.dto;

import java.util.ArrayList;
import java.util.List;

public class TopicList {
    private final List<TopicListing> topics;

    public TopicList(final List<TopicListing> topics) {
        this.topics = topics;
    }

    public List<TopicListing> getTopics() {
        return topics;
    }

    public List<String> getTopicNames() {
        final List<String> topicNames = new ArrayList<>();
        for (final TopicListing topicListing: getTopics()) {
            topicNames.add(topicListing.getName());
        }
        return topicNames;
    }

    @Override
    public String toString() {
        return "TopicList{" +
            "topics=" + topics +
            '}';
    }
}
