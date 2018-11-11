package org.sourcelab.kafka.webview.ui.controller.api.requests;

/**
 * Defines a request to remove a consumer.
 */
public class ConsumerRemoveRequest {
    private Long clusterId;
    private String consumerId;

    public ConsumerRemoveRequest() {
    }

    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(final Long clusterId) {
        this.clusterId = clusterId;
    }

    public String getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(final String consumerId) {
        this.consumerId = consumerId;
    }

    @Override
    public String toString() {
        return "ConsumerRemoveRequest{"
            + "clusterId='" + clusterId + '\''
            + ", consumerId='" + consumerId + '\''
            + '}';
    }
}
