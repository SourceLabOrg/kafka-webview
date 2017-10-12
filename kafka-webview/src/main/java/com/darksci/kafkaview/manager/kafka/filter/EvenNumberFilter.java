package com.darksci.kafkaview.manager.kafka.filter;

import java.util.Map;

public class EvenNumberFilter implements Filter {
    @Override
    public boolean filter(final String topic, final int partition, final long offset, final Object key, final Object value) {
        if (value == null) {
            return false;
        }
        final Number number = (Number) value;
        return number.longValue() > 600;
    }

    @Override
    public void configure(final Map<String, ?> configs) {

    }
}
