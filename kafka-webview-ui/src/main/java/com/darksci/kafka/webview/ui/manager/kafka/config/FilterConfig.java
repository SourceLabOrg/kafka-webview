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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Configuration defining any Filters that should be applied.
 */
public class FilterConfig {
    private List<RecordFilterDefinition> recordFilterDefinitions;

    /**
     * Constructor.
     * @param recordFilterDefinitions Which filters to apply.
     */
    public FilterConfig(final List<RecordFilterDefinition> recordFilterDefinitions) {
        this.recordFilterDefinitions = Collections.unmodifiableList(recordFilterDefinitions);
    }

    private FilterConfig() {
        recordFilterDefinitions = new ArrayList<>();
    }

    public List<RecordFilterDefinition> getFilters() {
        return recordFilterDefinitions;
    }

    /**
     * Factory method for creating instance with no filters.
     */
    public static FilterConfig withNoFilters() {
        return new FilterConfig();
    }

    /**
     * Factory method for creating an instance with one or more filters.
     */
    public static FilterConfig withFilters(final RecordFilterDefinition... recordFilterDefinitions) {
        return new FilterConfig(Arrays.asList(recordFilterDefinitions));
    }

    /**
     * Factory method to create an instance with one or more filters.
     */
    public static FilterConfig withFilters(final List<RecordFilterDefinition> recordFilterDefinitions) {
        return new FilterConfig(recordFilterDefinitions);
    }

    @Override
    public String toString() {
        return "FilterConfig{"
            + "recordFilterDefinitions=" + recordFilterDefinitions
            + '}';
    }
}
