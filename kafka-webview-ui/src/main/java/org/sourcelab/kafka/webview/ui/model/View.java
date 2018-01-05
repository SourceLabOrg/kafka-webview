/**
 * MIT License
 *
 * Copyright (c) 2017, 2018 SourceLab.org (https://github.com/Crim/kafka-webview/)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.sourcelab.kafka.webview.ui.model;

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
    private String partitions = "";

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

        // Avoid NPE.
        if (getPartitions() == null) {
            return partitionsSet;
        }

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
