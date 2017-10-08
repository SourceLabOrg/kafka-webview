package com.darksci.kafkaview.manager.kafka.filter;

import java.util.Map;

public interface Filter {
    boolean filter(final String topic, final int partition, final long offset, final Object key, final Object value);

    void configure(final Map<String, ?> configs);
}
