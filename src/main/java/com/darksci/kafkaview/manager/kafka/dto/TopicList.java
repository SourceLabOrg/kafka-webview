package com.darksci.kafkaview.manager.kafka.dto;

import java.util.List;

public class TopicList {
    private final List<TopicDetails> topicDetailsList;

    public TopicList(final List<TopicDetails> topicDetailsList) {
        this.topicDetailsList = topicDetailsList;
    }

    public List<TopicDetails> getAllTopics() {
        return topicDetailsList;
    }

    @Override
    public String toString() {
        return "TopicList{" +
            "topicDetailsList=" + topicDetailsList +
            '}';
    }
}
