package org.sourcelab.kafka.webview.ui.manager.user;

/**
 * Represents permissions in the application based on functional area.
 */
public enum Permissions {
    // Cluster
    CLUSTER_READ,
    CLUSTER_CREATE,
    CLUSTER_MODIFY,
    CLUSTER_DELETE,

    // View
    VIEW_READ,
    VIEW_CREATE,
    VIEW_MODIFY,
    VIEW_DELETE,

    // Topic
    TOPIC_READ,
    TOPIC_CREATE,
    TOPIC_MODIFY,
    TOPIC_DELETE,

    // Consumers
    CONSUMERS_READ,
    CONSUMERS_MODIFY,
    CONSUMERS_DELETE,

    // Users
    USERS_READ,
    USERS_CREATE,
    USERS_MODIFY,
    USERS_DELETE
}
