package org.sourcelab.kafka.webview.ui.manager.user.permission;

/**
 * Represents permissions in the application based on functional area.
 */
public enum Permissions {
    // Cluster
    CLUSTER_READ("Read"),
    CLUSTER_CREATE("Create"),
    CLUSTER_MODIFY("Update"),
    CLUSTER_DELETE("Delete"),

    // View
    VIEW_READ("Read"),
    VIEW_CREATE("Create"),
    VIEW_MODIFY("Update"),
    VIEW_DELETE("Delete"),

    // Topic
    TOPIC_READ("Read"),
    TOPIC_CREATE("Create"),
    TOPIC_MODIFY("Update"),
    TOPIC_DELETE("Delete"),

    // Consumers
    CONSUMER_READ("Read"),
    CONSUMER_MODIFY("Update"),
    CONSUMER_DELETE("Delete"),

    // Users
    USER_READ("Read"),
    USER_CREATE("Create"),
    USER_MODIFY("Update"),
    USER_DELETE("Delete");

    private final String displayName;

    Permissions(final String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
