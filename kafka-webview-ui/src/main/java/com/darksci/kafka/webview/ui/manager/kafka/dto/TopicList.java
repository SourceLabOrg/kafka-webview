package com.darksci.kafka.webview.ui.manager.kafka.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a collection of Topics within a Cluster.
 */
public class TopicList {
    private final List<TopicListing> topics;

    /**
     * Constructor.
     */
    public TopicList(final List<TopicListing> topics) {
        this.topics = topics;
    }

    /**
     * @return a List of topics.
     */
    public List<TopicListing> getTopics() {
        return Collections.unmodifiableList(topics);
    }

    /**
     * @return a List of the topic names.
     */
    public List<String> getTopicNames() {
        final List<String> topicNames = new ArrayList<>();
        for (final TopicListing topicListing: getTopics()) {
            topicNames.add(topicListing.getName());
        }
        return Collections.unmodifiableList(topicNames);
    }

    @Override
    public String toString() {
        return "TopicList{"
            + "+ topics=" + topics
            + '}';
    }
}
