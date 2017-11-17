package org.sourcelab.kafka.webview.ui.manager.kafka.config;

import org.apache.kafka.common.serialization.Deserializer;

/**
 * Configuration defining how to Deserialize values from Kafka.
 */
public class DeserializerConfig {
    private final Class<? extends Deserializer> keyDeserializerClass;
    private final Class<? extends Deserializer> valueDeserializerClass;

    /**
     * Constructor.
     * @param keyDeserializerClass Class for deserializer for keys.
     * @param valueDeserializerClass Class for deserializer for values.
     */
    public DeserializerConfig(
        final Class<? extends Deserializer> keyDeserializerClass,
        final Class<? extends Deserializer> valueDeserializerClass
    ) {
        this.keyDeserializerClass = keyDeserializerClass;
        this.valueDeserializerClass = valueDeserializerClass;
    }

    public Class<? extends Deserializer> getKeyDeserializerClass() {
        return keyDeserializerClass;
    }

    public Class<? extends Deserializer> getValueDeserializerClass() {
        return valueDeserializerClass;
    }

    @Override
    public String toString() {
        return "DeserializerConfig{"
            + "keyDeserializerClass=" + keyDeserializerClass
            + ", valueDeserializerClass=" + valueDeserializerClass
            + '}';
    }
}
