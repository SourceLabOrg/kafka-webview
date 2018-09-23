/**
 * MIT License
 *
 * Copyright (c) 2017, 2018 SourceLab.org (https://github.com/Crim/kafka-webview/)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.sourcelab.kafka.webview.ui.manager.kafka;

import org.sourcelab.kafka.webview.ui.controller.api.requests.ConsumeRequest;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.FilterDefinition;
import org.sourcelab.kafka.webview.ui.manager.socket.StartingPosition;
import org.sourcelab.kafka.webview.ui.model.Filter;
import org.sourcelab.kafka.webview.ui.model.View;
import org.sourcelab.kafka.webview.ui.model.ViewToFilterOptional;

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

    /**
     * Determine the appropriate starting position based on the request.
     * @return Where to resume consuming from.
     */
    public StartingPosition getStartingPosition() {
        final String requestedAction = consumeRequest.getAction();

        switch (requestedAction) {
            case "head":
                return StartingPosition.newHeadPosition();
            case "tail":
                return StartingPosition.newTailPosition();
            case "timestamp":
                return StartingPosition.newPositionFromTimestamp(consumeRequest.getTimestamp());
            case "offsets":
                // todo
            default:
                // Fall back to resume from existing
                return StartingPosition.newResumeFromExistingState();
        }
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
