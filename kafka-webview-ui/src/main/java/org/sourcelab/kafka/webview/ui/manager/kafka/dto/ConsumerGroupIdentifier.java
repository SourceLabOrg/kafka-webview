package org.sourcelab.kafka.webview.ui.manager.kafka.dto;

/**
 * Represents information about a consumer group identifier.
 */
public class ConsumerGroupIdentifier {
    private final String id;
    private final boolean isSimple;

    /**
     * Constructor.
     * @param id consumer group id/name.
     * @param isSimple If its a simple consumer or not.
     */
    public ConsumerGroupIdentifier(final String id, final boolean isSimple) {
        this.id = id;
        this.isSimple = isSimple;
    }

    public String getId() {
        return id;
    }

    public boolean isSimple() {
        return isSimple;
    }

    @Override
    public String toString() {
        return "ConsumerGroupIdentifier{"
            + "id='" + id + '\''
            + ", isSimple=" + isSimple
            + '}';
    }
}
