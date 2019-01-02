/**
 * MIT License
 *
 * Copyright (c) 2017, 2018 SourceLab.org (https://github.com/Crim/kafka-webview/)
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

package org.sourcelab.kafka.webview.ui.plugin.serializer;


import org.apache.kafka.common.serialization.Serializer;

import java.util.Collection;
import java.util.Map;

/**
 * For translation of values input from Kafka WebView UI/Browser, to a Kafka serializer.
 *
 * Typically an implementation of this interface is only needed in situations where you want to serialize
 * complex/composite objects which require multiple input values from the browser interface.
 *
 * @param <T> Object type the Kafka Serializer instance will be serializing.
 */
public interface SerializerTransformer<T> {
    /**
     * Configure this class.
     * @param configs configs in key/value pairs
     * @param isKey whether is for key or value
     */
    void configure(final Map<String, ?> configs, boolean isKey);

    /**
     * Transformation logic.
     * @param topic The topic being produced to.
     * @param valueMap Map of values to produce.
     * @return Serialized/flattened value that will get passed to the serializer instance.
     */
    T transform(final String topic, final Map<String, String> valueMap);

    /**
     * Underlying Kafka value serializer class.
     * @return Underlying Kafka value serializer class.
     */
    Class<? extends Serializer> getSerializerClass();

    /**
     * Return collection of field names to collect values from the web interface.
     * @return Collection of file names.
     */
    Collection<String> getFieldNames();
}
