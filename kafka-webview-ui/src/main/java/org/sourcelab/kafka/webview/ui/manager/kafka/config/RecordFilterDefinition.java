package org.sourcelab.kafka.webview.ui.manager.kafka.config;

import org.sourcelab.kafka.webview.ui.plugin.filter.RecordFilter;

import java.util.Collections;
import java.util.Map;

/**
 * Represents a Filter definition, the pairing of the RecordFilter instance plus any user defined options.
 */
public class RecordFilterDefinition {
    private final RecordFilter recordFilter;
    private final Map<String, String> options;

    /**
     * Constructor.
     * @param recordFilter The record filter instance.
     * @param options Any user defined options for the instance.
     */
    public RecordFilterDefinition(final RecordFilter recordFilter, final Map<String, String> options) {
        this.recordFilter = recordFilter;
        this.options = Collections.unmodifiableMap(options);
    }

    public RecordFilter getRecordFilter() {
        return recordFilter;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    @Override
    public String toString() {
        return "RecordFilterDefinition{"
            + "recordFilter=" + recordFilter
            + ", options=" + options
            + '}';
    }
}
