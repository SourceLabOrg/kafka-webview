package com.darksci.kafka.webview.manager.kafka.dto;

public class TopicListing {
    private final String name;
    private final boolean isInternal;

    public TopicListing(final String name, final boolean isInternal) {
        this.name = name;
        this.isInternal = isInternal;
    }

    public String getName() {
        return name;
    }

    public boolean isInternal() {
        return isInternal;
    }

    @Override
    public String toString() {
        return "TopicListing{" +
            "name='" + name + '\'' +
            ", isInternal=" + isInternal +
            '}';
    }
}
