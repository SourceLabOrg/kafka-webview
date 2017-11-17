package org.sourcelab.kafka.webview.ui.manager.kafka.dto;

/**
 * Represents a Topic from a cluster, and associated metadata.
 */
public class TopicListing {
    private final String name;
    private final boolean isInternal;

    /**
     * Constructor.
     */
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
        return "TopicListing{"
            + "+ name='" + name + '\''
            + ", + isInternal=" + isInternal
            + '}';
    }
}
