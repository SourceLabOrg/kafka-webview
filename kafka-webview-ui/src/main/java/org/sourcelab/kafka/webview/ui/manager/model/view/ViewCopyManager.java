/**
 * MIT License
 *
 * Copyright (c) 2017-2021 SourceLab.org (https://github.com/SourceLabOrg/kafka-webview/)
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

package org.sourcelab.kafka.webview.ui.manager.model.view;

import org.sourcelab.kafka.webview.ui.model.View;
import org.sourcelab.kafka.webview.ui.model.ViewToFilterEnforced;
import org.sourcelab.kafka.webview.ui.model.ViewToFilterOptional;
import org.sourcelab.kafka.webview.ui.repository.ViewRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * For copying 'view' entities.
 */
public class ViewCopyManager {

    private final ViewRepository viewRepository;

    /**
     * Constructor.
     * @param viewRepository View Reposistory dependency.
     */
    @Autowired
    public ViewCopyManager(final ViewRepository viewRepository) {
        this.viewRepository = viewRepository;
    }

    /**
     * Copy a view from a given view.id.
     * @param sourceViewId the id of the view to copy.
     * @param newViewName The name of the new copied view.
     * @return copied View instance.
     */
    public View copy(final long sourceViewId, final String newViewName) {
        // Retrieve view
        final Optional<View> sourceView = viewRepository.findById(sourceViewId);
        if (!sourceView.isPresent()) {
            throw new IllegalArgumentException("Unable to find view with id " + sourceViewId);
        }
        return copy(sourceView.get(), newViewName);
    }

    /**
     * Copy a view from a given view instance.
     * @param sourceView The view to copy.
     * @param newViewName The name of the new copied view.
     * @return copied View instance.
     */
    public View copy(final View sourceView, final String newViewName) {
        // Copy properties.
        final View copiedView = new View();
        copiedView.setName(newViewName);
        copiedView.setPartitions(sourceView.getPartitions());
        copiedView.setResultsPerPartition(sourceView.getResultsPerPartition());
        copiedView.setTopic(sourceView.getTopic());
        copiedView.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        copiedView.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        // Set foreign key references
        copiedView.setCluster(sourceView.getCluster());
        copiedView.setKeyMessageFormat(sourceView.getKeyMessageFormat());
        copiedView.setValueMessageFormat(sourceView.getValueMessageFormat());

        // Copy Many-to-Many relationships.

        // Enforced filters
        final Set<ViewToFilterEnforced> copiedEnforcedFilters = new HashSet<>();
        for (final ViewToFilterEnforced sourceFilter: sourceView.getEnforcedFilters()) {
            final ViewToFilterEnforced copiedFilter = new ViewToFilterEnforced();
            copiedFilter.setFilter(sourceFilter.getFilter());
            copiedFilter.setOptionParameters(sourceFilter.getOptionParameters());
            copiedFilter.setSortOrder(sourceFilter.getSortOrder());
            copiedFilter.setView(copiedView);
            copiedEnforcedFilters.add(copiedFilter);
        }
        if (!copiedEnforcedFilters.isEmpty()) {
            copiedView.setEnforcedFilters(copiedEnforcedFilters);
        }

        // Optional filters
        final Set<ViewToFilterOptional> copiedOptionalFilters = new HashSet<>();
        for (final ViewToFilterOptional sourceFilter: sourceView.getOptionalFilters()) {
            final ViewToFilterOptional copiedFilter = new ViewToFilterOptional();
            copiedFilter.setFilter(sourceFilter.getFilter());
            copiedFilter.setSortOrder(sourceFilter.getSortOrder());
            copiedFilter.setView(copiedView);
            copiedOptionalFilters.add(copiedFilter);
        }
        if (!copiedOptionalFilters.isEmpty()) {
            copiedView.setOptionalFilters(copiedOptionalFilters);
        }

        // Persist view and related entries.
        viewRepository.save(copiedView);

        // Return copy.
        return copiedView;
    }
}
