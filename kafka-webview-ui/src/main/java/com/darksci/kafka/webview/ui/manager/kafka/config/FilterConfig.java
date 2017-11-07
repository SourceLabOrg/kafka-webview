package com.darksci.kafka.webview.ui.manager.kafka.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Configuration defining any Filters that should be applied.
 */
public class FilterConfig {
    private List<RecordFilterDefinition> recordFilterDefinitions;

    /**
     * Constructor.
     * @param recordFilterDefinitions Which filters to apply.
     */
    public FilterConfig(final List<RecordFilterDefinition> recordFilterDefinitions) {
        this.recordFilterDefinitions = Collections.unmodifiableList(recordFilterDefinitions);
    }

    private FilterConfig() {
        recordFilterDefinitions = new ArrayList<>();
    }

    public List<RecordFilterDefinition> getFilters() {
        return recordFilterDefinitions;
    }

    /**
     * Factory method for creating instance with no filters.
     */
    public static FilterConfig withNoFilters() {
        return new FilterConfig();
    }

    /**
     * Factory method for creating an instance with one or more filters.
     */
    public static FilterConfig withFilters(final RecordFilterDefinition... recordFilterDefinitions) {
        return new FilterConfig(Arrays.asList(recordFilterDefinitions));
    }

    /**
     * Factory method to create an instance with one or more filters.
     */
    public static FilterConfig withFilters(final List<RecordFilterDefinition> recordFilterDefinitions) {
        return new FilterConfig(recordFilterDefinitions);
    }

    @Override
    public String toString() {
        return "FilterConfig{"
            + "recordFilterDefinitions=" + recordFilterDefinitions
            + '}';
    }
}
