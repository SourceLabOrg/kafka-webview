package com.example.myplugins;

import com.darksci.kafkaview.plugin.filter.RecordFilter;

import java.util.Map;

/**
 * Example implementation to only show records that do NOT have the value "A"
 */
public class ARecordFilter implements RecordFilter {

    @Override
    public boolean filter(final String topic, final int partition, final long offset, final Object key, final Object value) {
        return !("A".equals(key));
    }

    @Override
    public void configure(final Map<String, ?> configs) {

    }
}
