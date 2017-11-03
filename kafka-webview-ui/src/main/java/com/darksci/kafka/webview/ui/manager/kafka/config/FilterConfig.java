package com.darksci.kafka.webview.ui.manager.kafka.config;

import com.darksci.kafka.webview.ui.plugin.filter.RecordFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Configuration defining any Filters that should be applied.
 */
public class FilterConfig {
    private List<RecordFilter> recordFilters;

    /**
     * Constructor.
     * @param recordFilters Which filters to apply.
     */
    public FilterConfig(final List<RecordFilter> recordFilters) {
        this.recordFilters = Collections.unmodifiableList(recordFilters);
    }

    private FilterConfig() {
        recordFilters = new ArrayList<>();
    }

    public List<RecordFilter> getFilters() {
        return recordFilters;
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
    public static FilterConfig withFilters(final RecordFilter... recordFilters) {
        return new FilterConfig(Arrays.asList(recordFilters));
    }

    /**
     * Factory method to create an instance with one or more filters.
     */
    public static FilterConfig withFilters(final List<RecordFilter> recordFilters) {
        return new FilterConfig(recordFilters);
    }

    @Override
    public String toString() {
        return "FilterConfig{"
            + "recordFilters=" + recordFilters
            + '}';
    }
}
