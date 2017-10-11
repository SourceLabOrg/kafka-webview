package com.darksci.kafkaview.manager.kafka.filter;

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

public class FilterInterceptor implements ConsumerInterceptor {
    private final static Logger logger = LoggerFactory.getLogger(FilterInterceptor.class);
    public final static String CONFIG_KEY = "FilterInterceptor.Classes";

    private List<Filter> filters = new ArrayList<>();

    public FilterInterceptor() {

    }

    @Override
    public ConsumerRecords onConsume(ConsumerRecords records) {

        final Map<TopicPartition, List<ConsumerRecord>> filteredRecords = new HashMap<>();

        // Iterate thru records
        final Iterator<ConsumerRecord> recordIterator = records.iterator();
        while (recordIterator.hasNext()) {
            final ConsumerRecord record = recordIterator.next();

            // Iterate thru filters
            for (final Filter filter: filters) {
                // Pass through filter
                final boolean result = filter.filter(
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    record.key(),
                    record.value()
                );

                // If filter return true
                if (result) {
                    // Include it in the results
                    final TopicPartition topicPartition = new TopicPartition(record.topic(), record.partition());
                    filteredRecords.putIfAbsent(topicPartition, new ArrayList<>());
                    filteredRecords.get(topicPartition).add(record);
                }
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
        final Iterable<Class<? extends Filter>> filterClasses = (Iterable<Class<? extends Filter>>) configs.get(CONFIG_KEY);

        // Create instances fo filters
        for (final Class<? extends Filter> filterClass: filterClasses) {
            try {
                // Create instance
                final Filter filter = filterClass.newInstance();

                // Configure
                filter.configure(configs);

                // Add to list
                filters.add(filter);
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
