package com.darksci.kafka.webview.ui.manager.kafka.config;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

public class DeserializerConfig {
    private final Class<? extends Deserializer> keyDeserializerClass;
    private final Class<? extends Deserializer> valueDeserializerClass;

    public DeserializerConfig(final Class<? extends Deserializer> keyDeserializerClass, final Class<? extends Deserializer> valueDeserializerClass) {
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
        return "DeserializerConfig{" +
            "keyDeserializerClass=" + keyDeserializerClass +
            ", valueDeserializerClass=" + valueDeserializerClass +
            '}';
    }

    public static DeserializerConfig defaultConfig() {
        return new DeserializerConfig(StringDeserializer.class, StringDeserializer.class);
    }
}
