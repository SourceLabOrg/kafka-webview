package org.sourcelab.kafka.webview.ui.manager.kafka.config;

import org.sourcelab.kafka.webview.ui.model.Filter;

import java.util.Collections;
import java.util.Map;

/**
 * Represents a Filter and its options to be configured with a view.
 */
public class FilterDefinition {
    private final Filter filter;
    private final Map<String, String> options;

    /**
     * Constructor.
     * @param filter The filter entity..
     * @param options Any user defined options for the instance.
     */
    public FilterDefinition(final Filter filter, final Map<String, String> options) {
        this.filter = filter;
        this.options = Collections.unmodifiableMap(options);
    }

    public Filter getFilter() {
        return filter;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    @Override
    public String toString() {
        return "FilterDefinition{"
            + "filter=" + filter
            + ", options=" + options
            + '}';
    }
}
