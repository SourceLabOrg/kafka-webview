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
    private List<FilterDefinition> filterDefinitions;

    /**
     * Constructor.
     * @param filterDefinitions Which filters to apply.
     */
    public FilterConfig(final List<FilterDefinition> filterDefinitions) {
        this.filterDefinitions = Collections.unmodifiableList(filterDefinitions);
    }

    private FilterConfig() {
        filterDefinitions = new ArrayList<>();
    }

    public List<FilterDefinition> getFilters() {
        return filterDefinitions;
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
    public static FilterConfig withFilters(final FilterDefinition... filterDefinitions) {
        return new FilterConfig(Arrays.asList(filterDefinitions));
    }

    /**
     * Factory method to create an instance with one or more filters.
     */
    public static FilterConfig withFilters(final List<FilterDefinition> filterDefinitions) {
        return new FilterConfig(filterDefinitions);
    }

    @Override
    public String toString() {
        return "FilterConfig{"
            + "filterDefinitions=" + filterDefinitions
            + '}';
    }
}
