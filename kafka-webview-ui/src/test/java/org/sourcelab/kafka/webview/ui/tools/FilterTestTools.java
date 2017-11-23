package org.sourcelab.kafka.webview.ui.tools;

import org.sourcelab.kafka.webview.ui.model.Filter;
import org.sourcelab.kafka.webview.ui.repository.FilterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Helpful tools for Filters in tests.
 */
@Component
public class FilterTestTools {
    private final FilterRepository filterRepository;

    @Autowired
    public FilterTestTools(final FilterRepository filterRepository) {
        this.filterRepository = filterRepository;
    }

    /**
     * Utility for creating Filters.
     * @param name Name of the filter.
     * @return Persisted Filter.
     */
    public Filter createFiler(final String name) {
        final Filter filter = new Filter();
        filter.setName(name);
        filter.setClasspath("com.example." + name);
        filter.setJar(name + ".jar");
        filter.setOptions("{\"key\": \"value\"}");
        filterRepository.save(filter);

        return filter;
    }
}
