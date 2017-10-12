package com.darksci.kafkaview.manager.kafka.config;

import com.darksci.kafkaview.manager.kafka.filter.Filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FilterConfig {
    private List<Class<? extends Filter>> filters;

    public FilterConfig(final List<Class<? extends Filter>> filters) {
        this.filters = filters;
    }

    private FilterConfig() {
        filters = new ArrayList<>();
    }

    public List<Class<? extends Filter>> getFilters() {
        return filters;
    }

    public String getFiltersString() {
        return filters.stream()
            .map(Class::getName)
            .collect(Collectors.joining(","));
    }

    @Override
    public String toString() {
        return "FilterConfig{" +
            "filters=" + filters +
            '}';
    }

    public static FilterConfig withNoFilters() {
        return new FilterConfig();
    }

    public static FilterConfig withFilters(final Class<? extends Filter>... filterClasses) {
        return new FilterConfig(Arrays.asList(filterClasses));
    }
}
