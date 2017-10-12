package com.example.myplugins;

import com.darksci.kafkaview.plugin.filter.RecordFilter;

import java.util.Map;

/**
 * Example filter that filters even numbered offsets.
 */
public class EvenNumberRecordFilter implements RecordFilter {
    @Override
    public boolean filter(final String topic, final int partition, final long offset, final Object key, final Object value) {
        return (offset % 2 == 0);
    }

    @Override
    public void configure(final Map<String, ?> configs) {

    }
}
