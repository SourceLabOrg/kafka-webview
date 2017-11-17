package org.sourcelab.kafka.webview.ui.controller.api;

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
