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
package org.sourcelab.kafka.webview.ui.manager.kafka.producer.transformer;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Abstract implementation for default serializers.
 */
abstract public class DefaultTransformer<T> implements ValueTransformer<T> {

    private final static String defaultFieldName = "value";
    private final static Collection<String> defaultFieldNames = Collections.singletonList(defaultFieldName);

    /**
     * Transformation logic.
     * @param valueMap
     * @return
     */
    public T transform(final Map<String, String> valueMap) {
        final String value = valueMap.getOrDefault(defaultFieldName, null);
        if (value == null) {
            return null;
        }
        return transformField(valueMap.get(defaultFieldName));
    }

    public abstract T transformField(@NotNull String value);

    /**
     * Return collection of field names to collect values for.
     * @return Collection of file names.
     */
    public Collection<String> getFieldNames() {
        return defaultFieldNames;
    }

    public static Map<String, String> createDefaultValueMap(final String value) {
        return Collections.singletonMap(defaultFieldName, value);
    }
}
