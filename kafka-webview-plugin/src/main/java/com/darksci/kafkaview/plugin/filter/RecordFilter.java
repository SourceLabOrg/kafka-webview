package com.darksci.kafkaview.plugin.filter;

import java.util.Map;

/**
 * Interface that defines a Record Filter.
 */
public interface RecordFilter {
    boolean filter(final String topic, final int partition, final long offset, final Object key, final Object value);

    void configure(final Map<String, ?> configs);
}
