/**
 * MIT License
 *
 * Copyright (c) 2017 Stephen Powis https://github.com/Crim/kafka-webview
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

package com.darksci.kafka.webview.ui.controller.api;

import java.util.List;
import java.util.Map;

/**
 * Represents an API Request to consume.
 */
public class ConsumeRequest {
    private String action;
    private String partitions;
    private Integer resultsPerPartition;
    private List<Filter> filters;

    public String getAction() {
        return action;
    }

    public void setAction(final String action) {
        this.action = action;
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

    public List<Filter> getFilters() {
        return filters;
    }

    public void setFilters(final List<Filter> filters) {
        this.filters = filters;
    }

    /**
     * Represents a Filter with its parameters as requested.
     */
    public static class Filter {
        private Long filterId;
        private Map<String, String> options;

        public Long getFilterId() {
            return filterId;
        }

        public void setFilterId(final Long filterId) {
            this.filterId = filterId;
        }

        public Map<String, String> getOptions() {
            return options;
        }

        public void setOptions(final Map<String, String> options) {
            this.options = options;
        }

        @Override
        public String toString() {
            return "FilterDefinition{"
                + "filterId=" + filterId
                + ", options=" + options
                + '}';
        }
    }

    @Override
    public String toString() {
        return "ConsumeRequest{"
            + "action='" + action + '\''
            + ", partitions='" + partitions + '\''
            + ", resultsPerPartition=" + resultsPerPartition
            + ", filters=" + filters
            + '}';
    }
}
