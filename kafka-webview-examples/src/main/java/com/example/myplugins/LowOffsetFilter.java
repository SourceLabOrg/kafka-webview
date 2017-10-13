package com.example.myplugins;

import com.darksci.kafkaview.plugin.filter.RecordFilter;

import java.util.Map;

/**
 * Example filter that removes low offsets.
 */
public class LowOffsetFilter implements RecordFilter {
    @Override
    public boolean filter(final String topic, final int partition, final long offset, final Object key, final Object value) {
        return offset > 600;
    }

    @Override
    public void configure(final Map<String, ?> configs) {
    }
}
