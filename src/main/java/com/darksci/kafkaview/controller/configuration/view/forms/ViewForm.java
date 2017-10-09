package com.darksci.kafkaview.controller.configuration.view.forms;

import com.darksci.kafkaview.model.Cluster;
import com.darksci.kafkaview.model.MessageFormat;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class ViewForm {
    private Long id = null;

    @NotNull(message = "Enter a unique name")
    @Size(min = 2, max = 255)
    private String name;

    @NotNull(message = "Select a cluster")
    private Long clusterId;

    @NotNull(message = "Select a message format")
    private Long keyMessageFormatId;

    @NotNull(message = "Select a message format")
    private Long valueMessageFormatId;

    @NotNull(message = "Select a topic")
    @Size(min = 1, max = 255)
    private String topic;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(final Long clusterId) {
        this.clusterId = clusterId;
    }

    public Long getKeyMessageFormatId() {
        return keyMessageFormatId;
    }

    public void setKeyMessageFormatId(final Long keyMessageFormatId) {
        this.keyMessageFormatId = keyMessageFormatId;
    }

    public Long getValueMessageFormatId() {
        return valueMessageFormatId;
    }

    public void setValueMessageFormatId(final Long valueMessageFormatId) {
        this.valueMessageFormatId = valueMessageFormatId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(final String topic) {
        this.topic = topic;
    }

    public boolean exists() {
        return getId() != null;
    }

    @Override
    public String toString() {
        return "ViewForm{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", clusterId=" + clusterId +
            ", keyMessageFormatId=" + keyMessageFormatId +
            ", valueMessageFormatId=" + valueMessageFormatId +
            ", topic='" + topic + '\'' +
            '}';
    }
}
