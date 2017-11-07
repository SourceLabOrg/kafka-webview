package com.darksci.kafka.webview.ui.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Transient;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a record from the view table.
 */
@Entity
public class View {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    private Cluster cluster;

    @ManyToOne(fetch = FetchType.LAZY)
    private MessageFormat keyMessageFormat;

    @ManyToOne(fetch = FetchType.LAZY)
    private MessageFormat valueMessageFormat;

    @Column(nullable = false)
    private String topic;

    @Column(nullable = false)
    private String partitions;

    @Column(nullable = false)
    private Integer resultsPerPartition = 10;

    @OneToMany(
        fetch = FetchType.LAZY,
        cascade = { CascadeType.PERSIST, CascadeType.MERGE },
        mappedBy = "view",
        orphanRemoval = true)
    @OrderColumn(name = "sort_order")
    private Set<ViewToFilterEnforced> enforcedFilters = new HashSet<>();

    @OneToMany(
        fetch = FetchType.LAZY,
        cascade = { CascadeType.PERSIST, CascadeType.MERGE },
        mappedBy = "view",
        orphanRemoval = true)
    @OrderColumn(name = "sort_order")
    private Set<ViewToFilterOptional> optionalFilters = new HashSet<>();

    @Column(nullable = false)
    private Timestamp createdAt;

    @Column(nullable = false)
    private Timestamp updatedAt;

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

    public Cluster getCluster() {
        return cluster;
    }

    public void setCluster(final Cluster cluster) {
        this.cluster = cluster;
    }

    public MessageFormat getKeyMessageFormat() {
        return keyMessageFormat;
    }

    public void setKeyMessageFormat(final MessageFormat keyMessageFormat) {
        this.keyMessageFormat = keyMessageFormat;
    }

    public MessageFormat getValueMessageFormat() {
        return valueMessageFormat;
    }

    public void setValueMessageFormat(final MessageFormat valueMessageFormat) {
        this.valueMessageFormat = valueMessageFormat;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(final String topic) {
        this.topic = topic;
    }

    public String getPartitions() {
        return partitions;
    }

    public void setPartitions(final String partitions) {
        this.partitions = partitions;
    }

    public Integer getResultsPerPartition() {
        return resultsPerPartition;
    }

    public void setResultsPerPartition(final Integer resultsPerPartition) {
        this.resultsPerPartition = resultsPerPartition;
    }

    /**
     * @return Returns the defined partitions as a Set.
     */
    @Transient
    public Set<Integer> getPartitionsAsSet() {
        final Set<Integer> partitionsSet = new HashSet<>();
        final String[] partitions = getPartitions().split(",");

        for (final String partitionStr: partitions) {
            try {
                partitionsSet.add(Integer.parseInt(partitionStr));
            } catch (NumberFormatException e) {
                // Ignore?
            }
        }
        return partitionsSet;
    }

    public Set<ViewToFilterEnforced> getEnforcedFilters() {
        return enforcedFilters;
    }

    public void setEnforcedFilters(final Set<ViewToFilterEnforced> filters) {
        this.enforcedFilters = filters;
    }

    public Set<ViewToFilterOptional> getOptionalFilters() {
        return optionalFilters;
    }

    public void setOptionalFilters(final Set<ViewToFilterOptional> optionalFilters) {
        this.optionalFilters = optionalFilters;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(final Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}
