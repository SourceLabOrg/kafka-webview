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

package org.sourcelab.kafka.webview.ui.manager.kafka.filter;

import org.apache.kafka.clients.consumer.ConsumerInterceptor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sourcelab.kafka.webview.ui.manager.kafka.config.RecordFilterDefinition;
import org.sourcelab.kafka.webview.ui.plugin.filter.RecordFilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * RecordFilter Interceptor.  Used to apply 'server-side' filtering.
 */
public class RecordFilterInterceptor implements ConsumerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(RecordFilterInterceptor.class);
    public static final String CONFIG_KEY = "RecordFilterInterceptor.recordFilterDefinitions";

    private final List<RecordFilterDefinition> recordFilterDefinitions = new ArrayList<>();

    @Override
    public ConsumerRecords onConsume(final ConsumerRecords records) {

        final Map<TopicPartition, List<ConsumerRecord>> filteredRecords = new HashMap<>();

        // Iterate thru records
        final Iterator<ConsumerRecord> recordIterator = records.iterator();
        while (recordIterator.hasNext()) {
            final ConsumerRecord record = recordIterator.next();

            boolean result = true;

            // Iterate through filters
            for (final RecordFilterDefinition recordFilterDefinition : recordFilterDefinitions) {
                // Pass through filter
                result = recordFilterDefinition.getRecordFilter().filter(
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    record.key(),
                    record.value()
                );

                // If we return false
                if (!result) {
                    // break out of loop
                    break;
                }
            }

            // If filter return true
            if (result) {
                // Include it in the results
                final TopicPartition topicPartition = new TopicPartition(record.topic(), record.partition());
                filteredRecords.putIfAbsent(topicPartition, new ArrayList<>());
                filteredRecords.get(topicPartition).add(record);
            }
        }

        // return filtered results
        return new ConsumerRecords(filteredRecords);
    }

    @Override
    public void close() {
        // Call close on each filter.
        for (final RecordFilterDefinition recordFilterDefinition : recordFilterDefinitions) {
            recordFilterDefinition.getRecordFilter().close();
        }
    }

    @Override
    public void onCommit(final Map offsets) {
        // Nothing!
    }

    @Override
    public void configure(final Map<String, ?> consumerConfigs) {
        // Make immutable copy.
        final Map<String, ?> immutableConsumerConfigs = Collections.unmodifiableMap(consumerConfigs);

        // Grab definitions out of config
        final Iterable<RecordFilterDefinition> filterDefinitionsCfg = (Iterable<RecordFilterDefinition>) consumerConfigs.get(CONFIG_KEY);

        // Loop over
        for (final RecordFilterDefinition recordFilterDefinition : filterDefinitionsCfg) {
            try {
                // Grab filter and options
                final RecordFilter recordFilter = recordFilterDefinition.getRecordFilter();
                final Map<String, String> filterOptions = recordFilterDefinition.getOptions();

                // Configure it
                recordFilter.configure(immutableConsumerConfigs, filterOptions);

                // Add to list
                recordFilterDefinitions.add(recordFilterDefinition);
            } catch (final Exception exception) {
                logger.error(exception.getMessage(), exception);
            }
        }
    }
}
