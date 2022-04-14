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

import org.sourcelab.kafka.webview.ui.model.Filter;

import java.util.Collections;
import java.util.Map;

/**
 * Represents a Filter and its options to be configured with a view.
 */
public class FilterDefinition {
    private final Filter filter;
    private final Map<String, String> options;

    /**
     * Constructor.
     * @param filter The filter entity..
     * @param options Any user defined options for the instance.
     */
    public FilterDefinition(final Filter filter, final Map<String, String> options) {
        this.filter = filter;
        this.options = Collections.unmodifiableMap(options);
    }

    public Filter getFilter() {
        return filter;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    @Override
    public String toString() {
        return "FilterDefinition{"
            + "filter=" + filter
            + ", options=" + options
            + '}';
    }
}
