/**
 * MIT License
 *
 * Copyright (c) 2017, 2018, 2019 SourceLab.org (https://github.com/SourceLabOrg/kafka-webview/)
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

package org.sourcelab.kafka.webview.ui.controller.configuration.view.forms;

import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents the Create/Update View Form.
 */
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
        return "ViewForm{"
            + "id=" + id
            + ", name='" + name + '\''
            + ", clusterId=" + clusterId
            + ", keyMessageFormatId=" + keyMessageFormatId
            + ", valueMessageFormatId=" + valueMessageFormatId
            + ", topic='" + topic + '\''
            + ", partitions=" + partitions
            + ", enforcedFilters=" + enforcedFilters
            + ", optionalFilters=" + optionalFilters
            + ", resultsPerPartition=" + resultsPerPartition
            + '}';
    }
}
