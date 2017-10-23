package com.darksci.kafka.webview.plugin.examples.filter;

import com.darksci.kafka.webview.plugin.filter.RecordFilter;

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
