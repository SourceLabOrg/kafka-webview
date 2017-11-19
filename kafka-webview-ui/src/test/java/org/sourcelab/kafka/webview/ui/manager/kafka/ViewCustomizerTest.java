/**
 * MIT License
 *
 * Copyright (c) 2017 SourceLab.org (https://github.com/Crim/kafka-webview/)
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

import com.google.common.collect.Sets;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.sourcelab.kafka.webview.ui.controller.api.ConsumeRequest;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.FilterDefinition;
import org.sourcelab.kafka.webview.ui.manager.socket.StartingPosition;
import org.sourcelab.kafka.webview.ui.model.Filter;
import org.sourcelab.kafka.webview.ui.model.View;
import org.sourcelab.kafka.webview.ui.model.ViewToFilterOptional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ViewCustomizerTest {

    /**
     * Test we can override the view.
     */
    @Test
    public void testOverrideView() {
        // Create view
        final View view = new View();
        view.setPartitions("");
        view.setTopic("MyTopic");
        view.setResultsPerPartition(10);

        // Create consume request
        final ConsumeRequest consumeRequest = new ConsumeRequest();
        consumeRequest.setPartitions("2,3");
        consumeRequest.setResultsPerPartition(100);

        // Create instance & override
        final ViewCustomizer viewCustomizer = new ViewCustomizer(view, consumeRequest);
        viewCustomizer.overrideViewSettings();

        assertEquals("Should have 100 results per partition", 100, (int) view.getResultsPerPartition());
        assertEquals("should have partitions 2 and 3", "2,3", view.getPartitions());
        assertEquals("should have partitions 2 and 3", 2, view.getPartitionsAsSet().size());
        assertTrue("should have partitions 2 and 3", view.getPartitionsAsSet().contains(2));
        assertTrue("should have partitions 2 and 3", view.getPartitionsAsSet().contains(3));
    }

    /**
     * Test we can override the view when we have restricted partitions.
     */
    @Test
    public void testOverrideViewWithHasRestrictedPartitions() {
        // Create view
        final View view = new View();
        view.setPartitions("2,3,4");

        // Create consume request
        final ConsumeRequest consumeRequest = new ConsumeRequest();
        consumeRequest.setPartitions("2,3");

        // Create instance & override
        final ViewCustomizer viewCustomizer = new ViewCustomizer(view, consumeRequest);
        viewCustomizer.overrideViewSettings();

        assertEquals("should have partitions 2 and 3", "2,3", view.getPartitions());
        assertEquals("should have partitions 2 and 3", 2, view.getPartitionsAsSet().size());
        assertTrue("should have partitions 2 and 3", view.getPartitionsAsSet().contains(2));
        assertTrue("should have partitions 2 and 3", view.getPartitionsAsSet().contains(3));
    }

    /**
     * Test we can override the view when we have restricted partitions.
     */
    @Test
    public void testOverrideViewWithHasRestrictedPartitionsIllegalOptionIgnored() {
        // Create view
        final View view = new View();
        view.setPartitions("2,3,4");

        // Create consume request
        final ConsumeRequest consumeRequest = new ConsumeRequest();
        consumeRequest.setPartitions("2,3,10");

        // Create instance & override
        final ViewCustomizer viewCustomizer = new ViewCustomizer(view, consumeRequest);
        viewCustomizer.overrideViewSettings();

        assertEquals("should have partitions 2 and 3", "2,3", view.getPartitions());
        assertEquals("should have partitions 2 and 3", 2, view.getPartitionsAsSet().size());
        assertTrue("should have partitions 2 and 3", view.getPartitionsAsSet().contains(2));
        assertTrue("should have partitions 2 and 3", view.getPartitionsAsSet().contains(3));
    }

    /**
     * No filter options.
     */
    @Test
    public void testGetFilterDefinitionsEmpty() {
        // Create view
        final View view = new View();

        // Create consume request
        final ConsumeRequest consumeRequest = new ConsumeRequest();

        // Create instance & override
        final ViewCustomizer viewCustomizer = new ViewCustomizer(view, consumeRequest);
        List<FilterDefinition> filterDefinitions = viewCustomizer.getFilterDefinitions();

        assertTrue("Empty list", filterDefinitions.isEmpty());
    }

    /**
     * No filter options.
     */
    @Test
    public void testGetFilterDefinitions() {
        // Create view
        final View view = new View();

        // Create two filters
        final Filter filter1 = new Filter();
        filter1.setId(1);
        filter1.setName("Filter1");
        filter1.setClasspath("classpath1");
        filter1.setJar("jar1");

        // Create two filters
        final Filter filter2 = new Filter();
        filter2.setId(2);
        filter2.setName("Filter2");
        filter2.setClasspath("classpath2");
        filter2.setJar("jar2");

        // Associate them as optional filters
        final ViewToFilterOptional optional1 = new ViewToFilterOptional();
        optional1.setView(view);
        optional1.setFilter(filter1);

        // Associate them as optional filters
        final ViewToFilterOptional optional2 = new ViewToFilterOptional();
        optional2.setView(view);
        optional2.setFilter(filter2);

        view.setOptionalFilters(Sets.newHashSet(optional1, optional2));

        // Create consume request
        final ConsumeRequest consumeRequest = new ConsumeRequest();
        final Map<String, String> uiFilter1Options = new HashMap<>();
        uiFilter1Options.put("key1", "value1");
        uiFilter1Options.put("key2", "value2");

        final ConsumeRequest.Filter uiFilter1 = new ConsumeRequest.Filter();
        uiFilter1.setFilterId(1L);
        uiFilter1.setOptions(uiFilter1Options);

        consumeRequest.setFilters(Lists.newArrayList(uiFilter1));

        // Create instance & override
        final ViewCustomizer viewCustomizer = new ViewCustomizer(view, consumeRequest);
        List<FilterDefinition> filterDefinitions = viewCustomizer.getFilterDefinitions();

        assertFalse("Not Empty list", filterDefinitions.isEmpty());
        final FilterDefinition filterDefinition = filterDefinitions.get(0);
        Assert.assertEquals("Has expected filter", filter1, filterDefinition.getFilter());
        assertEquals("Has expected filter", uiFilter1Options, filterDefinition.getOptions());
    }

    /**
     * Test getting starting position when action set to tail.
     */
    @Test
    public void testStartingPositionTail() {
        // Create consume request
        final ConsumeRequest consumeRequest = new ConsumeRequest();
        consumeRequest.setAction("tail");

        // Create instance & override
        final ViewCustomizer viewCustomizer = new ViewCustomizer(new View(), consumeRequest);
        final StartingPosition startingPosition = viewCustomizer.getStartingPosition();

        // validate
        assertNotNull(startingPosition);
        assertTrue(startingPosition.isStartFromTail());
        assertFalse(startingPosition.isStartFromOffsets());
        assertFalse(startingPosition.isStartFromTimestamp());
        assertFalse(startingPosition.isStartFromHead());
    }

    /**
     * Test getting starting position when action set to head.
     */
    @Test
    public void testStartingPositionHead() {
        // Create consume request
        final ConsumeRequest consumeRequest = new ConsumeRequest();
        consumeRequest.setAction("head");

        // Create instance & override
        final ViewCustomizer viewCustomizer = new ViewCustomizer(new View(), consumeRequest);
        final StartingPosition startingPosition = viewCustomizer.getStartingPosition();

        // validate
        assertNotNull(startingPosition);
        assertFalse(startingPosition.isStartFromTail());
        assertFalse(startingPosition.isStartFromOffsets());
        assertFalse(startingPosition.isStartFromTimestamp());
        assertTrue(startingPosition.isStartFromHead());
    }

    /**
     * Test getting starting position when action set to timestamp.
     */
    @Test
    public void testStartingPositionTimestamp() {
        final long timestamp = 2432323L;

        // Create consume request
        final ConsumeRequest consumeRequest = new ConsumeRequest();
        consumeRequest.setAction("timestamp");
        consumeRequest.setTimestamp(timestamp);

        // Create instance & override
        final ViewCustomizer viewCustomizer = new ViewCustomizer(new View(), consumeRequest);
        final StartingPosition startingPosition = viewCustomizer.getStartingPosition();

        // validate
        assertNotNull(startingPosition);
        assertFalse(startingPosition.isStartFromTail());
        assertFalse(startingPosition.isStartFromOffsets());
        assertTrue(startingPosition.isStartFromTimestamp());
        assertFalse(startingPosition.isStartFromHead());
        assertEquals("Has expected timestamp", timestamp, startingPosition.getTimestamp());
    }
}