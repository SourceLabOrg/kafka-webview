/**
 * MIT License
 *
 * Copyright (c) 2017-2022 SourceLab.org (https://github.com/SourceLabOrg/kafka-webview/)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.sourcelab.kafka.webview.ui.manager.kafka.config;

import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration defining how to Deserialize values from Kafka.
 */
public class DeserializerConfig {

    private static final Logger logger = LoggerFactory.getLogger(DeserializerConfig.class);

    private final Class<? extends Deserializer> keyDeserializerClass;
    private final Map<String, String> keyDeserializerOptions;

    private final Class<? extends Deserializer> valueDeserializerClass;
    private final Map<String, String> valueDeserializerOptions;

    /**
     * Constructor.
     * @param keyDeserializerClass Class for deserializer for keys.
     * @param valueDeserializerClass Class for deserializer for values.
     */
    private DeserializerConfig(
        final Class<? extends Deserializer> keyDeserializerClass,
        final Map<String, String> keyDeserializerOptions,
        final Class<? extends Deserializer> valueDeserializerClass,
        final Map<String, String> valueDeserializerOptions
    ) {
        this.keyDeserializerClass = keyDeserializerClass;
        this.keyDeserializerOptions = new HashMap<>();
        this.keyDeserializerOptions.putAll(keyDeserializerOptions);

        this.valueDeserializerClass = valueDeserializerClass;
        this.valueDeserializerOptions = new HashMap<>();
        this.valueDeserializerOptions.putAll(valueDeserializerOptions);
    }

    public Class<? extends Deserializer> getKeyDeserializerClass() {
        return keyDeserializerClass;
    }

    public Class<? extends Deserializer> getValueDeserializerClass() {
        return valueDeserializerClass;
    }

    /**
     * @return Immutable map of options defined for key deserializer.
     */
    public Map<String, String> getKeyDeserializerOptions() {
        return Collections.unmodifiableMap(keyDeserializerOptions);
    }

    /**
     * @return Immutable map of options defined for value deserializer.
     */
    public Map<String, String> getValueDeserializerOptions() {
        return Collections.unmodifiableMap(valueDeserializerOptions);
    }

    /**
     * @return Mutable map of options defined for both the key and value deserializer.
     */
    public Map<String, String> getMergedOptions() {
        final Map<String, String> mergedOptions = new HashMap<>();
        mergedOptions.putAll(getKeyDeserializerOptions());
        mergedOptions.putAll(getValueDeserializerOptions());
        logger.warn("Merged options are {}", mergedOptions);
        return mergedOptions;
    }

    /**
     * @return Builder instance for DeserializerConfig.
     */
    public static DeserializerConfig.Builder newBuilder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "DeserializerConfig{"
            + "keyDeserializerClass=" + keyDeserializerClass
            + ", keyDeserializerOptions=" + keyDeserializerOptions
            + ", valueDeserializerClass=" + valueDeserializerClass
            + ", valueDeserializerOptions=" + valueDeserializerOptions
            + '}';
    }

    /**
     * Builder for DeserializerConfig.
     */
    public static final class Builder {
        private Class<? extends Deserializer> keyDeserializerClass;
        private Map<String, String> keyDeserializerOptions = new HashMap<>();

        private Class<? extends Deserializer> valueDeserializerClass;
        private Map<String, String> valueDeserializerOptions = new HashMap<>();

        private Builder() {
        }

        /**
         * Declare which Deserializer class to use to deserialize message key.
         * @param keyDeserializerClass Class to use for deserializing message key.
         * @return Builder instance.
         */
        public Builder withKeyDeserializerClass(Class<? extends Deserializer> keyDeserializerClass) {
            this.keyDeserializerClass = keyDeserializerClass;
            return this;
        }

        /**
         * Declare which Deserializer class to use to deserialize message value.
         * @param valueDeserializerClass Class to use for deserializing message value.
         * @return Builder instance.
         */
        public Builder withValueDeserializerClass(Class<? extends Deserializer> valueDeserializerClass) {
            this.valueDeserializerClass = valueDeserializerClass;
            return this;
        }

        /**
         * Add option to value deserializer.
         * @param key Key to set.
         * @param value Value to set.
         * @return Builder instance.
         */
        public Builder withValueDeserializerOption(final String key, final String value) {
            valueDeserializerOptions.put(key, value);
            return this;
        }

        /**
         * Add multiple options to value deserializer.
         * @param options Options to set.
         * @return Builder instance.
         */
        public Builder withValueDeserializerOptions(final Map<String, String> options) {
            valueDeserializerOptions.putAll(options);
            return this;
        }

        /**
         * Add option to key deserializer.
         * @param key Key to set.
         * @param value Value to set.
         * @return Builder instance.
         */
        public Builder withKeyDeserializerOption(final String key, final String value) {
            keyDeserializerOptions.put(key, value);
            return this;
        }

        /**
         * Add multiple options to key deserializer.
         * @param options Options to set.
         * @return Builder instance.
         */
        public Builder withKeyDeserializerOptions(final Map<String, String> options) {
            keyDeserializerOptions.putAll(options);
            return this;
        }

        /**
         * @return build DeserializerConfig instance.
         */
        public DeserializerConfig build() {
            return new DeserializerConfig(
                keyDeserializerClass,
                keyDeserializerOptions,
                valueDeserializerClass,
                valueDeserializerOptions
            );
        }
    }
}
