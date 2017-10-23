package com.darksci.kafkaview.plugin.filter;

import java.util.Map;

/**
 * Interface that defines a Record Filter.
 */
public interface RecordFilter {
    /**
     * Configure this class.
     * @param configs configs in key/value pairs
     */
    void configure(final Map<String, ?> configs);

    /**
     * Define the filter behavior.
     * A return value of TRUE means the record WILL be shown.
     * A return value of FALSE means the record will NOT be shown.
     *
     * @param topic Name of topic the message came from.
     * @param partition Partition the message came from.
     * @param offset Offset the message came from.
     * @param key Deserialized Key object.
     * @param value Deserialized Value object.
     * @return True means the record WILL be shown.  False means the record will NOT be shown.
     */
    boolean filter(final String topic, final int partition, final long offset, final Object key, final Object value);
}
