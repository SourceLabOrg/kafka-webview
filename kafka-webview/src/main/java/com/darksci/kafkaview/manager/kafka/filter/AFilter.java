package com.darksci.kafkaview.manager.kafka.filter;

import java.util.Map;

public class AFilter implements Filter {

    @Override
    public boolean filter(final String topic, final int partition, final long offset, final Object key, final Object value) {
        return !("A".equals(key));
    }

    @Override
    public void configure(final Map<String, ?> configs) {

    }
}
