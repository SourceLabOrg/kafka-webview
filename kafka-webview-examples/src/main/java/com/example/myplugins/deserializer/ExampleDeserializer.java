package com.example.myplugins.deserializer;

import org.apache.kafka.common.serialization.Deserializer;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ExampleDeserializer implements Deserializer<String> {

    @Override
    public void configure(final Map<String, ?> configs, final boolean isKey) {
        // Not used in this implementation.
    }

    @Override
    public String deserialize(final String topic, final byte[] data) {
        // Convert to string
        final String stringVal = new String(data, StandardCharsets.UTF_8);

        // Prefix it
        return "Prefixed Value: " + stringVal;
    }

    @Override
    public void close() {
        // Not used in this implementation.
    }
}
