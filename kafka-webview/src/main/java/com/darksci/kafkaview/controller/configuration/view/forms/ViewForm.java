package com.darksci.kafkaview.controller.configuration.view.forms;

import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

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

    /**
     * Empty set means ALL partitions.
     */
    @NotNull
    private Set<Integer> partitions = new HashSet<>();

    /**
     * Empty set means NO filters.
     * These filters are enforced on the view and can not be removed.
     * These are good if you want to enforce what users can see.
     */
    @NotNull
    private Set<Long> enforcedFilters = new HashSet<>();

    /**
     * Empty set means NO filters.
     * These filters can be optionally enabled at consume/display time by the user.
     * These are good if you want to allow customizable filtering at consume time.
     */
    @NotNull
    private Set<Long> optionalFilters = new HashSet<>();

    @NotNull
    @Range(min = 1, max = 500)
    private Integer resultsPerPartition = 10;

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

    public Integer getResultsPerPartition() {
        return resultsPerPartition;
    }

    public void setResultsPerPartition(final Integer resultsPerPartition) {
        this.resultsPerPartition = resultsPerPartition;
    }

    public Set<Integer> getPartitions() {
        return partitions;
    }

    public void setPartitions(final Set<Integer> partitions) {
        this.partitions = partitions;
    }

    public Set<Long> getEnforcedFilters() {
        return enforcedFilters;
    }

    public void setEnforcedFilters(final Set<Long> filters) {
        this.enforcedFilters = filters;
    }

    public Set<Long> getOptionalFilters() {
        return optionalFilters;
    }

    public void setOptionalFilters(final Set<Long> optionalFilters) {
        this.optionalFilters = optionalFilters;
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
            ", partitions=" + partitions +
            ", enforcedFilters=" + enforcedFilters +
            ", optionalFilters=" + optionalFilters +
            ", resultsPerPartition=" + resultsPerPartition +
            '}';
    }
}
