/**
 * MIT License
 *
 * Copyright (c) 2017 Stephen Powis https://github.com/Crim/kafka-webview
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

package com.darksci.kafka.webview.ui.manager.kafka.config;

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
