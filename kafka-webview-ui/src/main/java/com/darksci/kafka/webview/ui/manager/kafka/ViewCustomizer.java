package com.darksci.kafka.webview.ui.manager.kafka;

import com.darksci.kafka.webview.ui.controller.api.ConsumeRequest;
import com.darksci.kafka.webview.ui.manager.kafka.config.FilterDefinition;
import com.darksci.kafka.webview.ui.model.Filter;
import com.darksci.kafka.webview.ui.model.View;
import com.darksci.kafka.webview.ui.model.ViewToFilterOptional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Used to override settings in a view.
 */
public class ViewCustomizer {
    private final View view;
    private final ConsumeRequest consumeRequest;

    /**
     * Constructor.
     * @param view The View to customize
     * @param consumeRequest The request used to override view properties.
     */
    public ViewCustomizer(final View view, final ConsumeRequest consumeRequest) {
        this.view = view;
        this.consumeRequest = consumeRequest;
    }

    /**
     * Override any settings in the view.
     * @return overridden view
     */
    public View overrideViewSettings() {
        overrideResultPerPartition();
        overridePartitions();
        return view;
    }

    /**
     * @return Return Filter Definitions constructed from a ConsumerRequest.
     */
    public List<FilterDefinition> getFilterDefinitions() {
        final List<ConsumeRequest.Filter> requestFilters = consumeRequest.getFilters();

        // Determine if we should apply record filters
        // but if the view has enforced record filtering, don't bypass its logic, add onto it.
        final List<FilterDefinition> configuredFilters = new ArrayList<>();
        if (requestFilters != null && !requestFilters.isEmpty()) {
            // Retrieve all available filters
            final Map<Long, Filter> allowedFilters = new HashMap<>();

            // Build list of allowed filters
            for (final ViewToFilterOptional allowedFilter : view.getOptionalFilters()) {
                allowedFilters.put(allowedFilter.getFilter().getId(), allowedFilter.getFilter());
            }

            // Convert the String array into an actual array
            for (final ConsumeRequest.Filter requestedFilter: requestFilters) {
                // Convert to a long
                final Long requestedFilterId = requestedFilter.getFilterId();
                final Map<String, String> requestedFilterOptions = requestedFilter.getOptions();

                // See if its an allowed filter
                if (!allowedFilters.containsKey(requestedFilterId)) {
                    // Skip not allowed filters
                    continue;
                }
                // Define it
                final Filter filter = allowedFilters.get(requestedFilterId);
                final FilterDefinition filterDefinition = new FilterDefinition(filter, requestedFilterOptions);

                // Configure it
                configuredFilters.add(filterDefinition);
            }
        }
        return configuredFilters;
    }

    private void overrideResultPerPartition() {
        final Integer resultsPerPartition = consumeRequest.getResultsPerPartition();

        // Optionally over ride results per partition, within reason.
        if (resultsPerPartition != null && resultsPerPartition > 0 && resultsPerPartition < 500) {
            // Override in view
            view.setResultsPerPartition(resultsPerPartition);
        }
    }

    private void overridePartitions() {
        final String partitions = consumeRequest.getPartitions();

        // Determine if we should apply filters over partitions
        // but if the view has enforced partition filtering, don't bypass its logic.
        if (partitions != null && !partitions.isEmpty()) {
            final boolean filterPartitions;
            if (view.getPartitions() == null || view.getPartitions().isEmpty()) {
                filterPartitions = false;
            } else {
                filterPartitions = true;
            }

            // Create a string of partitions
            final Set<Integer> allowedPartitions = view.getPartitionsAsSet();
            final Set<Integer> configuredPartitions = new HashSet<>();

            // Convert the String array into an actual array
            for (final String requestedPartitionStr: partitions.split(",")) {
                try {
                    // If its not an allowed partition skip it
                    final Integer requestedPartition = Integer.parseInt(requestedPartitionStr);
                    if (filterPartitions && !allowedPartitions.contains(requestedPartition)) {
                        continue;
                    }
                    configuredPartitions.add(requestedPartition);
                } catch (final NumberFormatException e) {
                    // Skip invalid partitions
                    continue;
                }
            }

            // Finally override config if we have something
            if (!configuredPartitions.isEmpty()) {
                view.setPartitions(configuredPartitions.stream().map(Object::toString).collect(Collectors.joining(",")));
            }
        }
    }
}
