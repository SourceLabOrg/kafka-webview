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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sourcelab.kafka.webview.ui.model.Cluster;
import org.sourcelab.kafka.webview.ui.model.Filter;
import org.sourcelab.kafka.webview.ui.model.MessageFormat;
import org.sourcelab.kafka.webview.ui.model.View;
import org.sourcelab.kafka.webview.ui.model.ViewToFilterEnforced;
import org.sourcelab.kafka.webview.ui.model.ViewToFilterOptional;
import org.sourcelab.kafka.webview.ui.repository.ViewRepository;
import org.sourcelab.kafka.webview.ui.tools.ClusterTestTools;
import org.sourcelab.kafka.webview.ui.tools.FilterTestTools;
import org.sourcelab.kafka.webview.ui.tools.MessageFormatTestTools;
import org.sourcelab.kafka.webview.ui.tools.ViewTestTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ViewCopyManagerTest {

    @Autowired
    private ViewTestTools viewTestTools;

    @Autowired
    private ClusterTestTools clusterTestTools;

    @Autowired
    private MessageFormatTestTools messageFormatTestTools;

    @Autowired
    private FilterTestTools filterTestTools;

    @Autowired
    private ViewRepository viewRepository;

    /**
     * Simple test case of copying a view, no many to many relationships.
     */
    @Test
    @Transactional
    public void basicCopyTest() {
        final String originalViewName = "My Original View";
        final String partitions = "1,2,4";
        final String topic = "MyTopic";

        final String expectedCopyName = "My Copied View";

        final Cluster cluster = clusterTestTools.createCluster("Test Cluster");
        final MessageFormat keyMessageFormat = messageFormatTestTools.createMessageFormat("Key Message Format");
        final MessageFormat valueMessageFormat = messageFormatTestTools.createMessageFormat("Value Message Format");

        // Create source view
        final View sourceView = viewTestTools.createView(originalViewName, cluster, keyMessageFormat);
        sourceView.setKeyMessageFormat(keyMessageFormat);
        sourceView.setValueMessageFormat(valueMessageFormat);
        sourceView.setTopic(topic);
        sourceView.setPartitions(partitions);

        viewRepository.save(sourceView);

        // Attempt to copy
        final ViewCopyManager copyManager = new ViewCopyManager(viewRepository);
        final View copiedView = copyManager.copy(sourceView.getId(),expectedCopyName);

        // Validate properties
        assertNotNull("Should be non-null", copiedView);
        assertNotNull("Should have an id", copiedView.getId());
        assertNotEquals("Should have a new id", copiedView.getId(), sourceView.getId());
        assertEquals("Has new name", expectedCopyName, copiedView.getName());
        assertEquals("Has topic", topic, copiedView.getTopic());
        assertEquals("Has partitions", partitions, copiedView.getPartitions());
        assertEquals("Has cluster", cluster.getId(), copiedView.getCluster().getId());
        assertEquals("Has key format", keyMessageFormat.getId(), copiedView.getKeyMessageFormat().getId());
        assertEquals("Has value format", valueMessageFormat.getId(), copiedView.getValueMessageFormat().getId());
    }

    /**
     * Test case of copying a view with many to many relationships.
     */
    @Test
    @Transactional
    public void copyTest_WithManyToManyRelationships() {
        final String originalViewName = "My Original View";
        final String partitions = "1,2,4";
        final String topic = "MyTopic";

        final String expectedCopyName = "My Copied View";

        final Cluster cluster = clusterTestTools.createCluster("Test Cluster");
        final MessageFormat keyMessageFormat = messageFormatTestTools.createMessageFormat("Key Message Format");
        final MessageFormat valueMessageFormat = messageFormatTestTools.createMessageFormat("Value Message Format");
        final Filter filterA = filterTestTools.createFilter("Filter A");
        final Filter filterB = filterTestTools.createFilter("Filter B");
        final Filter filterC = filterTestTools.createFilter("Filter C");
        final Filter filterD = filterTestTools.createFilter("Filter D");

        // Create source view
        final View sourceView = viewTestTools.createView(originalViewName, cluster, keyMessageFormat);
        sourceView.setKeyMessageFormat(keyMessageFormat);
        sourceView.setValueMessageFormat(valueMessageFormat);
        sourceView.setTopic(topic);
        sourceView.setPartitions(partitions);

        viewRepository.save(sourceView);

        final ViewToFilterEnforced enforcedFilterA = addEnforcedFilterToView(sourceView, filterA, 1L);
        final ViewToFilterEnforced enforcedFilterB = addEnforcedFilterToView(sourceView, filterB, 2L);
        final ViewToFilterOptional enforcedFilterC = addOptionalFilterToView(sourceView, filterC,3L);
        final ViewToFilterOptional enforcedFilterD = addOptionalFilterToView(sourceView, filterD,4L);

        viewRepository.save(sourceView);

        // Attempt to copy
        final ViewCopyManager copyManager = new ViewCopyManager(viewRepository);
        View copiedView = copyManager.copy(sourceView.getId(),expectedCopyName);
        assertNotNull("Should be non-null", copiedView);
        assertNotNull("Should have an id", copiedView.getId());
        assertNotEquals("Should have a new id", copiedView.getId(), sourceView.getId());

        // Re-pull entity from database?
        copiedView = viewRepository.findById(copiedView.getId()).get();

        // Validate properties
        assertNotNull("Should have an id", copiedView.getId());
        assertEquals("Has new name", expectedCopyName, copiedView.getName());
        assertEquals("Has topic", topic, copiedView.getTopic());
        assertEquals("Has partitions", partitions, copiedView.getPartitions());
        assertEquals("Has cluster", cluster.getId(), copiedView.getCluster().getId());
        assertEquals("Has key format", keyMessageFormat.getId(), copiedView.getKeyMessageFormat().getId());
        assertEquals("Has value format", valueMessageFormat.getId(), copiedView.getValueMessageFormat().getId());

        // Validate enforced filters
        assertEquals("Should have 2 enforced filters", 2, copiedView.getEnforcedFilters().size());
        for (final ViewToFilterEnforced copiedFilter: copiedView.getEnforcedFilters()) {
            assertNotNull(copiedFilter.getId());
            assertNotEquals("Should have a new id", enforcedFilterA.getId(), copiedFilter.getId());
            assertNotEquals("Should have a new id", enforcedFilterB.getId(), copiedFilter.getId());
            assertNotEquals("Should have a new id", enforcedFilterC.getId(), copiedFilter.getId());
            assertNotEquals("Should have a new id", enforcedFilterD.getId(), copiedFilter.getId());

            final String expectedFilterName;
            final String expectedParameterOptions;
            if (copiedFilter.getSortOrder().equals(1L)) {
                expectedFilterName = "Filter A";
                expectedParameterOptions = "my parameters 1";
            } else if (copiedFilter.getSortOrder().equals(2L)) {
                expectedFilterName = "Filter B";
                expectedParameterOptions = "my parameters 2";
            } else {
                throw new RuntimeException("Invalid sort order found? " + copiedFilter.getSortOrder());
            }
            assertEquals(expectedParameterOptions, copiedFilter.getOptionParameters());
            assertEquals(expectedFilterName, copiedFilter.getFilter().getName());
        }

        assertEquals("Should have 2 optional filters", 2, copiedView.getOptionalFilters().size());
        for (final ViewToFilterOptional copiedFilter: copiedView.getOptionalFilters()) {
            assertNotNull(copiedFilter.getId());
            assertNotEquals("Should have a new id", enforcedFilterA.getId(), copiedFilter.getId());
            assertNotEquals("Should have a new id", enforcedFilterB.getId(), copiedFilter.getId());
            assertNotEquals("Should have a new id", enforcedFilterC.getId(), copiedFilter.getId());
            assertNotEquals("Should have a new id", enforcedFilterD.getId(), copiedFilter.getId());

            final String expectedFilterName;
            if (copiedFilter.getSortOrder().equals(3L)) {
                expectedFilterName = "Filter C";
            } else if (copiedFilter.getSortOrder().equals(4L)) {
                expectedFilterName = "Filter D";
            } else {
                throw new RuntimeException("Invalid sort order found? " + copiedFilter.getSortOrder());
            }
            assertEquals(expectedFilterName, copiedFilter.getFilter().getName());
        }
    }

    private ViewToFilterEnforced addEnforcedFilterToView(final View view, final Filter filter, final long sortOrder) {
        final ViewToFilterEnforced viewToFilter = new ViewToFilterEnforced();
        viewToFilter.setView(view);
        viewToFilter.setFilter(filter);
        viewToFilter.setSortOrder(sortOrder);
        viewToFilter.setOptionParameters("my parameters " + sortOrder);
        view.getEnforcedFilters().add(viewToFilter);
        return viewToFilter;
    }

    private ViewToFilterOptional addOptionalFilterToView(final View view, final Filter filter, final long sortOrder) {
        final ViewToFilterOptional viewToFilter = new ViewToFilterOptional();
        viewToFilter.setView(view);
        viewToFilter.setFilter(filter);
        viewToFilter.setSortOrder(sortOrder);
        view.getOptionalFilters().add(viewToFilter);
        return viewToFilter;
    }
}