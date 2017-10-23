package com.darksci.kafka.webview.manager.kafka.filter;

import com.darksci.kafka.webview.plugin.filter.RecordFilter;
import org.apache.kafka.clients.consumer.ConsumerInterceptor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RecordFilterInterceptor implements ConsumerInterceptor {
    private final static Logger logger = LoggerFactory.getLogger(RecordFilterInterceptor.class);
    public final static String CONFIG_KEY = "RecordFilterInterceptor.Classes";

    private List<RecordFilter> recordFilters = new ArrayList<>();

    public RecordFilterInterceptor() {

    }

    @Override
    public ConsumerRecords onConsume(ConsumerRecords records) {

        final Map<TopicPartition, List<ConsumerRecord>> filteredRecords = new HashMap<>();

        // Iterate thru records
        final Iterator<ConsumerRecord> recordIterator = records.iterator();
        while (recordIterator.hasNext()) {
            final ConsumerRecord record = recordIterator.next();

            boolean result = true;

            // Iterate thru filters
            for (final RecordFilter recordFilter: recordFilters) {
                // Pass through filter
                result = recordFilter.filter(
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
        // Not needed?
    }

    @Override
    public void onCommit(final Map offsets) {
        // Dunno yet.
    }

    @Override
    public void configure(final Map<String, ?> configs) {
        // Grab classes from config
        final Iterable<RecordFilter> filters = (Iterable<RecordFilter>) configs.get(CONFIG_KEY);

        // Create instances fo filters
        for (final RecordFilter recordFilter : filters) {
            try {
                // Configure
                recordFilter.configure(configs);

                // Add to list
                recordFilters.add(recordFilter);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
}
