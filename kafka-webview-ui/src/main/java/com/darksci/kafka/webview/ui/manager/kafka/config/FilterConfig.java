package com.darksci.kafka.webview.ui.manager.kafka.config;

import com.darksci.kafka.webview.ui.plugin.filter.RecordFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FilterConfig {
    private List<RecordFilter> recordFilters;

    public FilterConfig(final List<RecordFilter> recordFilters) {
        this.recordFilters = recordFilters;
    }

    private FilterConfig() {
        recordFilters = new ArrayList<>();
    }

    public List<RecordFilter> getFilters() {
        return recordFilters;
    }

    @Override
    public String toString() {
        return "FilterConfig{" +
            "recordFilters=" + recordFilters +
            '}';
    }

    public static FilterConfig withNoFilters() {
        return new FilterConfig();
    }

    public static FilterConfig withFilters(final RecordFilter... recordFilters) {
        return new FilterConfig(Arrays.asList(recordFilters));
    }

    public static FilterConfig withFilters(final List<RecordFilter> recordFilters) {
        return new FilterConfig(recordFilters);
    }
}
