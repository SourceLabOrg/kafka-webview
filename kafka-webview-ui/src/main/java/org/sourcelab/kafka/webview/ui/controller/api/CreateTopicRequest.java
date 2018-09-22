package org.sourcelab.kafka.webview.ui.controller.api;

public class CreateTopicRequest {
    private String name;
    private Integer partitions;
    private Short replicas;

    public void setName(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Integer getPartitions() {
        return partitions;
    }

    public void setPartitions(final Integer partitions) {
        this.partitions = partitions;
    }

    public Short getReplicas() {
        return replicas;
    }

    public void setReplicas(final Short replicas) {
        this.replicas = replicas;
    }

    @Override
    public String toString() {
        return "CreateTopicRequest{"
            + "name='" + name + '\''
            + ", partitions=" + partitions
            + ", replicas=" + replicas
            + '}';
    }
}
