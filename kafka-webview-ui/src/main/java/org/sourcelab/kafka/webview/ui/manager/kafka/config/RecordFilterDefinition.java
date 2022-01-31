/**
 * MIT License
 *
 * Copyright (c) 2017-2021 SourceLab.org (https://github.com/SourceLabOrg/kafka-webview/)
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

import org.sourcelab.kafka.webview.ui.plugin.filter.RecordFilter;

import java.util.Collections;
import java.util.Map;

/**
 * Represents a Filter definition, the pairing of the RecordFilter instance plus any user defined options.
 */
public class RecordFilterDefinition {
    private final RecordFilter recordFilter;
    private final Map<String, String> options;

    /**
     * Constructor.
     * @param recordFilter The record filter instance.
     * @param options Any user defined options for the instance.
     */
    public RecordFilterDefinition(final RecordFilter recordFilter, final Map<String, String> options) {
        this.recordFilter = recordFilter;
        this.options = Collections.unmodifiableMap(options);
    }

    public RecordFilter getRecordFilter() {
        return recordFilter;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    @Override
    public String toString() {
        return "RecordFilterDefinition{"
            + "recordFilter=" + recordFilter
            + ", options=" + options
            + '}';
    }
}
