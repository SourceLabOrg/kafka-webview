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

package org.sourcelab.kafka.webview.ui.manager.kafka.filter;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.RecordFilterDefinition;
import org.sourcelab.kafka.webview.ui.plugin.filter.RecordFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RecordFilterInterceptorTest {

    /**
     * Test that configured filters are configured and closed appropriately.
     */
    @Test
    public void testConfigureAndClose() {
        // Create mock Filter
        final RecordFilter mockFilter1 = mock(RecordFilter.class);
        final Map<String, String> filterOptions1 = new HashMap<>();
        filterOptions1.put("key1", "value1");
        filterOptions1.put("key2", "value2");

        final RecordFilterDefinition recordFilterDefinition1 = new RecordFilterDefinition(mockFilter1, filterOptions1);

        // Create 2nd mock Filter
        final RecordFilter mockFilter2 = mock(RecordFilter.class);
        final Map<String, String> filterOptions2 = new HashMap<>();
        filterOptions1.put("key3", "value3");
        filterOptions1.put("key4", "value4");

        final RecordFilterDefinition recordFilterDefinition2 = new RecordFilterDefinition(mockFilter2, filterOptions2);

        // Create ConsumerConfigs
        final Map<String, Object> consumerConfigs = new HashMap<>();
        consumerConfigs.put(RecordFilterInterceptor.CONFIG_KEY, Lists.newArrayList(recordFilterDefinition1, recordFilterDefinition2));

        // Create interceptor.
        final RecordFilterInterceptor interceptor = new RecordFilterInterceptor();

        // Call configure
        interceptor.configure(consumerConfigs);

        // Validate we called configure on the mock filters
        verify(mockFilter1, times(1)).configure(eq(consumerConfigs), eq(filterOptions1));
        verify(mockFilter2, times(1)).configure(eq(consumerConfigs), eq(filterOptions2));

        // Call close
        interceptor.close();

        // Validate we called close on the mock filters
        verify(mockFilter1, times(1)).close();
        verify(mockFilter2, times(1)).close();
    }

    /**
     * Test that filters can pass messages.
     */
    @Test
    public void testPassThrough() {
        final int totalRecords = 5;

        // Create mock Filters
        final RecordFilter mockFilter1 = mock(RecordFilter.class);
        final RecordFilter mockFilter2 = mock(RecordFilter.class);

        when(mockFilter1.includeRecord(eq("MyTopic"), eq(0), anyLong(), any(), any())).thenReturn(true);
        when(mockFilter2.includeRecord(eq("MyTopic"), eq(0), anyLong(), any(), any())).thenReturn(true);

        final RecordFilterDefinition recordFilterDefinition1 = new RecordFilterDefinition(mockFilter1, new HashMap<>());
        final RecordFilterDefinition recordFilterDefinition2 = new RecordFilterDefinition(mockFilter2, new HashMap<>());

        // Create ConsumerConfigs
        final Map<String, Object> consumerConfigs = new HashMap<>();
        consumerConfigs.put(RecordFilterInterceptor.CONFIG_KEY, Lists.newArrayList(recordFilterDefinition1, recordFilterDefinition2));

        // Create interceptor.
        final RecordFilterInterceptor interceptor = new RecordFilterInterceptor();

        // Call configure
        interceptor.configure(consumerConfigs);

        // Create ConsumerRecords
        final ConsumerRecords consumerRecords = createConsumerRecords(totalRecords);

        // Pass through interceptor
        final ConsumerRecords results = interceptor.onConsume(consumerRecords);

        // Validate we got the expected results
        assertEquals("Should have 5 records", totalRecords, results.count());

        // Verify mocks
        verify(mockFilter1, times(totalRecords))
            .includeRecord(eq("MyTopic"), eq(0), anyLong(), any(), any());
        verify(mockFilter2, times(totalRecords))
            .includeRecord(eq("MyTopic"), eq(0), anyLong(), any(), any());
    }

    /**
     * Test that filters can filter messages.
     */
    @Test
    public void testFilterMessages() {
        final int totalRecords = 5;

        // Create mock Filters
        final RecordFilter mockFilter1 = mock(RecordFilter.class);
        final RecordFilter mockFilter2 = mock(RecordFilter.class);

        when(mockFilter1.includeRecord(eq("MyTopic"), eq(0), anyLong(), any(), any()))
            .thenReturn(true, false, true, true, true);
        when(mockFilter2.includeRecord(eq("MyTopic"), eq(0), anyLong(), any(), any()))
            .thenReturn(true, true, false, true);

        final RecordFilterDefinition recordFilterDefinition1 = new RecordFilterDefinition(mockFilter1, new HashMap<>());
        final RecordFilterDefinition recordFilterDefinition2 = new RecordFilterDefinition(mockFilter2, new HashMap<>());

        // Create ConsumerConfigs
        final Map<String, Object> consumerConfigs = new HashMap<>();
        consumerConfigs.put(RecordFilterInterceptor.CONFIG_KEY, Lists.newArrayList(recordFilterDefinition1, recordFilterDefinition2));

        // Create interceptor.
        final RecordFilterInterceptor interceptor = new RecordFilterInterceptor();

        // Call configure
        interceptor.configure(consumerConfigs);

        // Create ConsumerRecords
        final ConsumerRecords consumerRecords = createConsumerRecords(totalRecords);

        // Pass through interceptor
        final ConsumerRecords results = interceptor.onConsume(consumerRecords);

        // Validate we got the expected results
        assertEquals("Should have 3 records", totalRecords - 2, results.count());

        for (Iterator<ConsumerRecord> it = results.iterator(); it.hasNext(); ) {
            final ConsumerRecord consumerRecord = it.next();
            assertNotEquals("Should not have offsets 1 and 3", 1, consumerRecord.offset());
            assertNotEquals("Should not have offsets 1 and 3", 3, consumerRecord.offset());
        }

        // Verify mocks
        verify(mockFilter1, times(totalRecords))
            .includeRecord(eq("MyTopic"), eq(0), anyLong(), any(), any());
        verify(mockFilter2, times(totalRecords - 1))
            .includeRecord(eq("MyTopic"), eq(0), anyLong(), any(), any());
    }

    private ConsumerRecords createConsumerRecords(final int count) {
        final String topic = "MyTopic";
        final int partition = 0;

        final Map<TopicPartition, List<ConsumerRecord>> recordsMap = new HashMap<>();
        final TopicPartition topicPartition = new TopicPartition(topic, partition);
        final List<ConsumerRecord> consumerRecords = new ArrayList<>();

        for (int x = 0; x < count; x++) {
            consumerRecords.add(
                new ConsumerRecord<Object, Object>(topic, partition, x, "Key" + x, "Value" + x)
            );
        }
        recordsMap.put(topicPartition, consumerRecords);

        return new ConsumerRecords(recordsMap);
    }
}